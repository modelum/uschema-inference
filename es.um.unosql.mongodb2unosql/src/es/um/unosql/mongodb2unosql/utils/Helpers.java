package es.um.unosql.mongodb2unosql.utils;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import scala.Tuple2;
import scala.Tuple3;

public class Helpers
{
	public static Document simplify(Document d)
	{
		Document simplified = new Document();
		for (String key : d.keySet())
		{
			Object value = simplify(d.get(key));
			simplified.put(key, value);
		}
		return simplified;
	}

	private static Object simplify(Object input)
	{
		Object ret = null;
		if (input instanceof String || input instanceof Date)
			ret = Constants.SIMPLE_DEFAULT_STRING;
		else if (input instanceof Boolean)
			ret = Constants.SIMPLE_DEFAULT_BOOLEAN;
		else if (input instanceof Integer)
			ret = Constants.SIMPLE_DEFAULT_INTEGER;
		else if (input instanceof Double)
			ret = Constants.SIMPLE_DEFAULT_DOUBLE;
		else if (input instanceof Long)
			ret = Constants.SIMPLE_DEFAULT_LONG;
		else if (input instanceof ObjectId)
			ret = Constants.SIMPLE_DEFAULT_OBJECTID;
		else if (input instanceof Document)
			ret = simplify((Document)input);
		else if (input instanceof ArrayList<?>) {
			ArrayList<?> elements = (ArrayList<?>)input;
			ArrayList<Object> retArray = new ArrayList<Object>();
			for (Object element : elements)
				retArray.add(simplify(element));
			ret = retArray;
		}
		else if (input != null )
			throw new java.lang.UnsupportedOperationException("Document field type not supported: " + input.getClass());

		return ret;
	}

	/**
	 * Generates a tuple whose key is a simplified version of de BSON Document
	 * @param doc BSON Document
	 * @return
	 */
	public static Tuple2<Document, Tuple3<Long, Long, Integer>> generateDocumentPair(Document doc)
	{
		long timestamp = doc.getObjectId("_id").getTimestamp();
		Tuple3<Long, Long, Integer> data = new Tuple3<Long, Long, Integer>(timestamp,timestamp,1);

		return new Tuple2<Document, Tuple3<Long, Long, Integer>> (simplify(doc), data);
	}

	/**
	 * Combines two pair values (FirstTimestamp, LastTimestamp, Count) into a single one.
	 *
	 * @param tuple1 First pair to combine
	 * @param tuple2 Second pair to combine
	 * @return
	 */
	public static Tuple3<Long, Long, Integer> reducePairs(Tuple3<Long, Long, Integer> tuple1, Tuple3<Long, Long, Integer> tuple2)
	{
		return new Tuple3<Long, Long, Integer>(
				Math.min(tuple1._1(), tuple2._1()),
				Math.max(tuple1._2(), tuple2._2()),
				tuple1._3() + tuple2._3());
	}

	/**
	 * Generates a valid JSON String from a pair with BSONDocument as key and (FirstTimestamp, LastTimestamp, Count) as value.
	 *
	 * @param input Pair (BSON Document, (FirstTimestamp, LastTimestamp, Count)
	 * @return
	 */
	public static String documentPairToJSONString(Tuple2<Document, Tuple3<Long, Long, Integer>> input)
	{
		return "{\"schema\":" + input._1().toJson() + "," +
				"\"count\":"+ input._2()._3() +"," +
				"\"firstTimestamp\":" + input._2()._1() + "," +
				"\"lastTimestamp\":"+input._2()._2() + "}";
	}

	public static JsonNode documentPairToJSONNode(Tuple2<Document, Tuple3<Long, Long, Integer>> input, ObjectMapper objectMapper)
	{
		ObjectNode ret = objectMapper.createObjectNode();

		try {
			ret.set("schema", objectMapper.readTree(input._1().toJson()));
			ret.put("count", input._2()._3());
			ret.put("firstTimestamp", input._2()._1());
			ret.put("lastTimestamp", input._2()._2());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

}

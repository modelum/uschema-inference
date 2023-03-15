package es.um.uschema.documents.extractors.util;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MapReduceIterable;

public class MongoDBStreamAdapter extends AbstractStreamAdapter
{
  public Stream<JsonObject> adaptStream(Map<String, MapReduceIterable<Document>> mapRedMap)
  {
    Stream<JsonObject> result = Stream.empty();

    result = mapRedMap.entrySet().stream().flatMap(e ->
    	StreamSupport.stream(e.getValue().spliterator(), true).map(doc ->
    	{
    		Document docValue = doc.get("value", Document.class);
    		JsonObject jObj = new JsonObject();
    		
    		jObj.add("schema", JsonParser.parseString(docValue.getString("schema")));
    		jObj.addProperty("count", docValue.getDouble("count").intValue());
    		jObj.addProperty("firstTimestamp", docValue.getDouble("firstTimestamp").longValue());
    		jObj.addProperty("lastTimestamp", docValue.getDouble("lastTimestamp").longValue());
    		jObj.getAsJsonObject("schema").addProperty("_type", e.getKey().substring(0, 1).toUpperCase() + e.getKey().substring(1));

    		return jObj;
    	}));

    return result;
  }
}

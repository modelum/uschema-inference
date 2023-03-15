package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.documents.injectors.adapters.DbClient;
import es.um.uschema.documents.injectors.util.DbType;

public class Json2Db extends Source2Db
{
	public Json2Db(DbType db, String ip)
	{
		super(db, ip);
	}

	public void run(File jsonRoute, String dbName)
	{
		long startTime = System.currentTimeMillis();

		System.out.println("Reading input model " + jsonRoute + "...");
		storeJSONContent(jsonRoute, dbName);
		System.out.println(jsonRoute.getName() + " table created in " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void storeJSONContent(File jsonRoute, String dbName)
	{
		DbClient client = getClient();
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayToInsert = mapper.createArrayNode();
			JsonNode objArray = mapper.readTree(jsonRoute);
			String collName = "";

			for (JsonNode node : objArray)
			{
				ObjectNode obj = (ObjectNode)node;
				if (!obj.has("_id"))
					obj.put("_id", new ObjectId().toString());

				String newCollName = obj.get("_type").asText();
				obj.remove("_type");

				if (!collName.equals(newCollName) && arrayToInsert.size() != 0)
				{
					client.insert(dbName, collName, arrayToInsert.toString());
					arrayToInsert.removeAll();
				}
				arrayToInsert.add(obj);
				collName = newCollName;
			}

			client.insert(dbName, collName, arrayToInsert.toString());

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
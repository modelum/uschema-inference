package es.um.uschema.documents.injectors.interfaces;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.documents.injectors.util.DbType;

public class EveryPolitician2Db extends Source2Db
{
  public EveryPolitician2Db(DbType db, String ip)
  {
    super(db, ip);
  }

  public void run(File jsonRoute, String dbName)
  {
    long startTime = System.currentTimeMillis();

    System.out.println("Reading json file " + jsonRoute + "...");
    storeJSONContent(jsonRoute, dbName);
    System.out.println(dbName + ":" + jsonRoute.getName() + " table created in " + (System.currentTimeMillis() - startTime) + " ms");
  }

  private void storeJSONContent(File jsonRoute, String dbName)
  {
    try
    {
      JsonNode rootObj = new ObjectMapper().readTree(jsonRoute);
      rootObj.fieldNames().forEachRemaining( fieldName ->
      {
        if (fieldName.equals("meta"))
          return;

        ArrayNode collection = (ArrayNode)rootObj.get(fieldName);
        collection.forEach(obj -> transformObject((ObjectNode)obj));

        if (collection.size() > 0)
          getClient().insert(dbName, fieldName, collection.toString());
      });
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void transformObject(ObjectNode obj)
  {
    if (obj.has("id"))
    {
      obj.put("_id", obj.get("id").asText());
      obj.remove("id");
    }
  }
}

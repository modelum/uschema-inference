package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.documents.injectors.util.DbType;

public class Hearthstone2Db extends Source2Db
{
  private int MAX_LINES_BEFORE_STORE = 25000;

  public Hearthstone2Db(DbType db, String ip)
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
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootObj = mapper.readTree(jsonRoute);
      String collectionName = "hearthstone";

      int numLines = 0;
      int totalLines = 1;
      ArrayNode jsonArray = mapper.createArrayNode();

      for (JsonNode obj : rootObj)
      {
        transformObject((ObjectNode)obj);
        jsonArray.add(obj);

        if (++numLines == MAX_LINES_BEFORE_STORE)
        {
          getClient().insert(dbName, collectionName, jsonArray.toString());
          jsonArray.removeAll();
          numLines = 0;
          System.out.println("Line count: " + totalLines);
        }

        totalLines++;
      }

      if (jsonArray.size() > 0)
      {
        System.out.println("Storing remaining files...");
        getClient().insert(dbName, collectionName, jsonArray.toString());
      }
    } catch (Exception e)
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

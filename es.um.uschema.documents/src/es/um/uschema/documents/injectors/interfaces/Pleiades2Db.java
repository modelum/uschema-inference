package es.um.uschema.documents.injectors.interfaces;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import es.um.uschema.documents.injectors.util.DbType;

public class Pleiades2Db extends Source2Db
{
  public Pleiades2Db(DbType db, String ip)
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
      ArrayNode collection = (ArrayNode) rootObj.get("@graph");
      ArrayNode partCollection = mapper.createArrayNode();
      int MAX_OBJECTS_TO_COMMIT = 1000;
      int countObjects = 0;
      int totalObjects = 0;

      for (JsonNode obj : collection)
      {
        partCollection.add(obj);

        if (++countObjects == MAX_OBJECTS_TO_COMMIT)
        {
          getClient().insert(dbName, "@graph", partCollection.toString());
          partCollection.removeAll();
          countObjects = 0;
          System.out.println("Object count: " + totalObjects);
        }

        totalObjects++;
      }

      if (partCollection.size() > 0)
      {
        System.out.println("Storing remaining objects...");
        getClient().insert(dbName, "@graph", partCollection.toString());
      }
    } catch (JsonProcessingException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
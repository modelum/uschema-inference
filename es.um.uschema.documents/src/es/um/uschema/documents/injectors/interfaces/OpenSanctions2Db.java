package es.um.uschema.documents.injectors.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.documents.injectors.util.DbType;

public class OpenSanctions2Db extends Source2Db
{
  private int MAX_LINES_BEFORE_STORE = 2000;

  public OpenSanctions2Db(DbType db, String ip)
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
    try (BufferedReader reader = new BufferedReader(new FileReader(jsonRoute)))
    {
      ObjectMapper mapper = new ObjectMapper();
      ArrayNode jsonArray = mapper.createArrayNode();
      String collectionName = "posts";
      int numLines = 0;
      int totalLines = 1;
      System.out.println("Storing each " + MAX_LINES_BEFORE_STORE);

      for (String line; (line = reader.readLine()) != null; totalLines++)
      {
        jsonArray.add(transformObject((ObjectNode)mapper.readTree(line)));

        if (++numLines == MAX_LINES_BEFORE_STORE)
        {
          getClient().insert(dbName, collectionName, jsonArray.toString());
          jsonArray.removeAll();
          numLines = 0;
          System.out.println("Line count: " + totalLines);
        }
      }

      if (jsonArray.size() > 0)
      {
        System.out.println("Storing remaining files...");
        getClient().insert(dbName, collectionName, jsonArray.toString());
      }
    } catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private ObjectNode transformObject(ObjectNode obj)
  {
    if (obj.has("id"))
    {
      obj.put("_id", obj.get("id").asText());
      obj.remove("id");
    }
    if (obj.has("identifiers"))
    {
      ArrayNode identifiers = (ArrayNode)obj.get("identifiers");
      identifiers.forEach(id ->
      {
        if (id.has("number") && id.get("number").asInt() != 0)
        {
          ObjectNode objId = (ObjectNode)id;
          objId.put("number", objId.get("number").asInt());
        }
      });
    }

    return obj;
  }
}
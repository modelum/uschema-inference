package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import es.um.uschema.documents.injectors.pojo.solar_system.Bodies;
import es.um.uschema.documents.injectors.util.DbType;

public class SolarSystem2Db extends Source2Db
{
  public SolarSystem2Db(DbType db, String ip)
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
    MappingIterator<?> mappingIterator = null;
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    String collectionName = null;

    try
    {
      mappingIterator = mapper.readerFor(es.um.uschema.documents.injectors.pojo.solar_system.System.class).readValues(jsonRoute);
      collectionName = "bodies";

      int numLines = 0;
      int totalLines = 1;
      int MAX_LINES_BEFORE_STORE = 1000;

      ArrayNode jsonArray = mapper.createArrayNode();

      es.um.uschema.documents.injectors.pojo.solar_system.System system = (es.um.uschema.documents.injectors.pojo.solar_system.System)mappingIterator.next();

      for (Bodies body : system.getBodies())
      {
        jsonArray.add(mapper.readTree(mapper.writeValueAsString(body)));

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
/*
    try
    {
      ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_EMPTY);
      JsonNode rootObj = mapper.readTree(new File(jsonRoute));
      ArrayNode collection = (ArrayNode) rootObj.get("bodies");
      ArrayNode partCollection = mapper.createArrayNode();
      int MAX_OBJECTS_TO_COMMIT = 1000;
      int countObjects = 0;
      int totalObjects = 0;

      for (JsonNode obj : collection)
      {
        partCollection.add(obj);

        if (++countObjects == MAX_OBJECTS_TO_COMMIT)
        {
          getClient().insert(dbName, "bodies", partCollection.toString());
          partCollection.removeAll();
          countObjects = 0;
          System.out.println("Object count: " + totalObjects);
        }

        totalObjects++;
      }

      if (partCollection.size() > 0)
      {
        System.out.println("Storing remaining objects...");
        getClient().insert(dbName, "bodies", partCollection.toString());
      }
    } catch (JsonProcessingException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }*/
  }
}
package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

import es.um.uschema.documents.injectors.pojo.webclick.Webclick;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.injectors.util.deserializer.NumberToNumberDeserializer;
import es.um.uschema.documents.injectors.util.deserializer.StringToStringDeserializer;

public class Webclick2Db extends Source2Db
{
  private int MAX_LINES_BEFORE_STORE = 5000;

  public Webclick2Db(DbType db, String ip)
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

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Integer.class, new NumberToNumberDeserializer());
    module.addDeserializer(String.class, new StringToStringDeserializer());
    mapper.registerModule(module);

    try
    {
      mappingIterator = mapper.readerFor(Webclick.class).readValues(jsonRoute);
      collectionName = "webclicks";

      int numLines = 0;
      int totalLines = 1;
      ArrayNode jsonArray = mapper.createArrayNode();

      while (mappingIterator.hasNext())
      {
        jsonArray.add(mapper.readTree(mapper.writeValueAsString(mappingIterator.next())));

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
}

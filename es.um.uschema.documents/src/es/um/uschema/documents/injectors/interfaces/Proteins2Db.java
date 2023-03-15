package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import es.um.uschema.documents.injectors.pojo.protein.Protein;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.injectors.util.deserializer.NumberToNumberDeserializer;
import es.um.uschema.documents.injectors.util.deserializer.StringToStringDeserializer;

public class Proteins2Db extends Source2Db
{
  private int MAX_LINES_BEFORE_STORE = 25000;

  public Proteins2Db(DbType db, String ip)
  {
    super(db, ip);
  }

  public void run(File csvRoute, String dbName)
  {
    long startTime = System.currentTimeMillis();

    System.out.println("Reading csv file " + csvRoute + "...");
    storeCSVContent(csvRoute, dbName);
    System.out.println(dbName + ":" + csvRoute.getName() + " table created in " + (System.currentTimeMillis() - startTime) + " ms");
  }

  private void storeCSVContent(File csvRoute, String dbName)
  {
    CsvMapper csvMapper = new CsvMapper();
    MappingIterator<?> mappingIterator = null;
    ObjectMapper oMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    String collectionName = null;

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Integer.class, new NumberToNumberDeserializer());
    module.addDeserializer(String.class, new StringToStringDeserializer());
    csvMapper.registerModule(module);

    try
    {
      mappingIterator = csvMapper.readerFor(Protein.class).with(CsvSchema.emptySchema().withHeader()).readValues(csvRoute);
      collectionName = "proteins";

      int numLines = 0;
      int totalLines = 1;
      ArrayNode jsonArray = oMapper.createArrayNode();

      while (mappingIterator.hasNext())
      {
        jsonArray.add(oMapper.readTree(oMapper.writeValueAsString(mappingIterator.next())));

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

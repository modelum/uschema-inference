package es.um.uschema.documents.injectors.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import es.um.uschema.documents.injectors.pojo.facebook.Comment;
import es.um.uschema.documents.injectors.pojo.facebook.Page;
import es.um.uschema.documents.injectors.pojo.facebook.Post;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.injectors.util.deserializer.NumberToNumberDeserializer;
import es.um.uschema.documents.injectors.util.deserializer.StringToStringDeserializer;

public class Facebook2Db extends Source2Db
{
  private int MAX_LINES_BEFORE_STORE = 25000;

  private HashMap<String, List<String>> uniqueKeys;

  public Facebook2Db(DbType db, String ip)
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
    uniqueKeys = new HashMap<String, List<String>>();
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
      if (csvRoute.getName().contains("post"))
      {
        uniqueKeys.put("posts", new ArrayList<String>());
        mappingIterator = csvMapper.readerFor(Post.class).with(CsvSchema.emptySchema().withHeader()).readValues(csvRoute);
        collectionName = "posts";
      }
      else if (csvRoute.getName().contains("pagename"))
      {
        uniqueKeys.put("pages", new ArrayList<String>());
        mappingIterator = csvMapper.readerFor(Page.class).with(CsvSchema.emptySchema().withHeader()).readValues(csvRoute);
        collectionName = "pages";
      }
      else if (csvRoute.getName().contains("comment"))
      {
        mappingIterator = csvMapper.readerFor(Comment.class).with(CsvSchema.emptySchema().withHeader()).readValues(csvRoute);
        collectionName = "comments";
      }

      int numLines = 0;
      int totalLines = 1;
      ArrayNode jsonArray = oMapper.createArrayNode();

        while (mappingIterator.hasNext())
        {
          JsonNode objectToInsert = oMapper.readTree(oMapper.writeValueAsString(mappingIterator.next()));
          if (transformObject(collectionName, (ObjectNode)objectToInsert))
            jsonArray.add(objectToInsert);

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

  private boolean transformObject(String collectionName, ObjectNode obj)
  {
    switch (collectionName)
    {
      case "posts":
      {
        obj.put("_id", obj.get("post_id").asText());
        obj.remove("post_id");
        break;
      }
      case "pages":
      {
        obj.put("_id", obj.get("page_id").asText());
        obj.remove("page_id");
        break;
      }
      default:
      {
        return true;
      }
    }

    if (uniqueKeys.get(collectionName).stream().anyMatch(id -> id.equals(obj.get("_id").asText())))
      return false;
    else
    {
      uniqueKeys.get(collectionName).add(obj.get("_id").asText());
      return true;
    }
  }
}

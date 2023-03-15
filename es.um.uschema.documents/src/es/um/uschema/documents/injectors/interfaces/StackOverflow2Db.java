package es.um.uschema.documents.injectors.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import es.um.uschema.documents.injectors.util.DbType;

public class StackOverflow2Db extends Source2Db
{
    private int MAX_LINES_BEFORE_STORE = 20000;

    public StackOverflow2Db(DbType db, String ip)
    {
        super(db, ip);
    }

    @Override
    public void run(File xmlRoute, String dbName)
    {
        long startTime = System.currentTimeMillis();

        System.out.println("Reading xml file " + xmlRoute + "...");
        storeXMLContent(xmlRoute, dbName);
        System.out.println(dbName + ":" + xmlRoute.getName() + " table created in "
                + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void storeXMLContent(File userXMLRoute, String dbName)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(userXMLRoute)))
        {
            ArrayNode jsonArray = new ObjectMapper().createArrayNode();
            int numLines = 0;
            int totalLines = 0;
            reader.readLine(); // XML header line
            String collectionName = reader.readLine(); // <collectionName> line

            if (collectionName.length() < 2)
            {
                System.out.println("Error reading the collection name on line 2.");
                System.exit(-1);
            } else
            {
                collectionName = collectionName.substring(1, collectionName.length() - 1);
                System.out.println("Inserting " + collectionName + " collection");
                System.out.println("Storing each " + MAX_LINES_BEFORE_STORE);
            }

            String previousLine = reader.readLine();

            for (String line; (line = reader.readLine()) != null; totalLines++)
            {
                jsonArray.add(adaptXMLLine(previousLine));

                previousLine = line;

                if (++numLines == MAX_LINES_BEFORE_STORE)
                {
                    getClient().insert(dbName, collectionName, jsonArray);
                    jsonArray = new ObjectMapper().createArrayNode();
                    numLines = 0;
                    System.out.println("Line count: " + totalLines);
                }
            }

            if (jsonArray.size() > 0)
            {
                System.out.println("Storing remaining files...");
                getClient().insert(dbName, collectionName, jsonArray);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ObjectNode adaptXMLLine(String line)
    {
        XmlMapper xmlMapper = new XmlMapper();

        ObjectNode objNode = null;
        try
        {
            objNode = xmlMapper.readValue(line, ObjectNode.class);

            var fieldsToRemove = new HashSet<String>();
            objNode.fields().forEachRemaining(f -> {
                if (f.getValue().asText().isEmpty())
                    fieldsToRemove.add(f.getKey());
            });
            objNode.remove(fieldsToRemove);

            // TODO: Dates
            String integerFields[] = { "PostTypeId", "ParentId", "AcceptedAnswerId", "Score", "ViewCount",
                    "OwnerUserId", "LastEditorUserId", "AnswerCount", "CommentCount", "FavoriteCount" };
            for (var field : integerFields)
                if (objNode.has(field))
                    objNode.put(field, objNode.get(field).asInt());

            objNode.put("_id", objNode.get("Id").asInt());
            objNode.remove("Id");

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return objNode;
    }
}

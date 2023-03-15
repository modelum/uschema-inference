package es.um.uschema.documents.injectors.adapters.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;

import es.um.uschema.documents.injectors.adapters.DbClient;

public class MongoDbClient implements DbClient
{
    MongoClient client;

    public MongoDbClient(String hostname, int port)
    {
        client = MongoClients.create("mongodb://" + hostname + ":" + port);
    }

    @Override
    public void insert(String dbName, ArrayNode node)
    {
        Map<String, List<Document>> collections = new HashMap<String, List<Document>>();
        // For each object create a collection of that type (obj.type) and remove the type attribute.

        int batchCounter = 0;
        int BATCHSIZE = 100000;

        for (JsonNode item : node)
        {
            String type = item.get("_type").asText();
            type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();

            if (!collections.containsKey(type))
                collections.put(type, new ArrayList<Document>());

            ObjectNode object = (ObjectNode) item;
            object.remove("_type");

            collections.get(type).add(Document.parse(object.toString()));

            if (batchCounter % BATCHSIZE == 0)
            {
                insert(dbName, collections);
                collections = new HashMap<String, List<Document>>();
            }

            batchCounter++;
        }

        insert(dbName, collections);
    }

    @Override
    public void insert(String dbName, String jsonContent)
    {
        Map<String, List<Document>> collections = new HashMap<String, List<Document>>();
        // For each object create a collection of that type (obj.type) and remove the type attribute.
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonItems = mapper.readTree(jsonContent);

            for (JsonNode item : jsonItems)
            {
                String type = item.get("_type").asText().toLowerCase();
                if (!collections.containsKey(type))
                    collections.put(type, new ArrayList<Document>());

                ObjectNode object = (ObjectNode) item;
                object.remove("_type");

                collections.get(type).add(Document.parse(object.toString()));
            }

            insert(dbName, collections);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(String dbName, String collectionName, String jsonContent)
    {
        Map<String, List<Document>> collection = new HashMap<String, List<Document>>();
        List<Document> docList = new ArrayList<Document>();

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonItems = mapper.readTree(jsonContent);

            jsonItems.forEach(jsonElement -> {
                docList.add(Document.parse(jsonElement.toString()));
            });

            collection.put(collectionName, docList);
            insert(dbName, collection);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void insert(String dbName, Map<String, List<Document>> collections)
    {
        MongoDatabase db = client.getDatabase(dbName);

        for (String collName : collections.keySet())
        {
            MongoCollection<Document> collection = db.getCollection(collName);
            insert(collection, collections.get(collName));
        }
    }

    private void insert(MongoCollection<Document> collection, List<Document> docs)
    {
        collection.insertMany(docs, new InsertManyOptions().bypassDocumentValidation(true).ordered(false));
    }

    @Override
    public void cleanDb(String dbName)
    {
        client.getDatabase(dbName).drop();
    }

    @Override
    public void cleanAllDbs()
    {
        for (String dbName : client.listDatabaseNames())
            if (!dbName.equals("admin") && !dbName.equals("local"))
                client.getDatabase(dbName).drop();
    }

    @Override
    public boolean shutdown()
    {
        client.close();

        return true;
    }

    public MongoDatabase getDatabase(String dbName)
    {
        return client.getDatabase(dbName);
    }

    @Override
    public void insert(String dbName, String collectionName, ArrayNode jsonArray)
    {
        List<Document> docList = new ArrayList<Document>();
        MongoDatabase db = client.getDatabase(dbName);
        jsonArray.forEach(jsonNode -> {
            docList.add(Document.parse(jsonNode.toString()));
        });

        MongoCollection<Document> collection = db.getCollection(collectionName);
        insert(collection, docList);
    }
}

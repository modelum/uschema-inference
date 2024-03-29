package es.um.uschema.documents.injectors.adapters.couchdb;

import java.util.ArrayList;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.documents.injectors.adapters.DbClient;

public class CouchDbClient extends StdCouchDbInstance implements DbClient
{
    public CouchDbClient(HttpClient client)
    {
        super(client);
    }

    @Override
    public void insert(String dbName, String jsonContent)
    {
        try
        {
            CouchDbConnector connector = createConnector(dbName, true);
            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonItems = mapper.readTree(jsonContent);
            List<JsonNode> itemList = new ArrayList<JsonNode>();

            if (jsonItems.isArray())
                for (JsonNode item : jsonItems)
                {
                    normalizeId(item);
                    normalizeType(item);
                    itemList.add(item);
                }

            connector.executeBulk(itemList);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(String dbName, String collectionName, String jsonContent)
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = null;

        try
        {
            arrayNode = (ArrayNode) mapper.readTree(jsonContent);

            arrayNode.forEach(jsonElement -> {
                normalizeId(jsonElement);
                normalizeType(jsonElement, collectionName);
            });

            insert(dbName, arrayNode.toString());

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanDb(String dbName)
    {
        try
        {
            deleteDatabase(dbName);
        } catch (DocumentNotFoundException e)
        {
            System.err.println("Database doesnt exist: " + dbName);
        }
    }

    @Override
    public void cleanAllDbs()
    {
        for (String dbName : getAllDatabases())
            deleteDatabase(dbName);
    }

    @Override
    public boolean shutdown()
    {
        getConnection().shutdown();
        return true;
    }

    private void normalizeId(JsonNode element)
    {
        JsonNode id = element.get("_id");

        if (id != null && id.isObject())
        {
            String newId = "";
            // The common way to store an id object is by the tag "$oid".
            // So we check that attribute first.
            if (id.has("$oid"))
                newId = id.get("$oid").asText();
            else
                // If it doesnt exist we just take the first field of the id object.
                newId = id.fields().next().getValue().asText();
            ((ObjectNode) element).put("_id", newId);
        }
    }

    private void normalizeType(JsonNode element)
    {
        normalizeType(element, element.get("_type").asText());
    }

    private void normalizeType(JsonNode element, String collectionName)
    {
        ((ObjectNode) element).put("type", collectionName);
        ((ObjectNode) element).remove("_type");
    }

    @Override
    public void insert(String dbName, ArrayNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void insert(String dbName, String collectionName, ArrayNode jsonArray)
    {
        // TODO Auto-generated method stub

    }
}

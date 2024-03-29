package es.um.uschema.documents.injectors.adapters;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface DbClient
{
    public void insert(String dbName, String jsonContent);

    public void insert(String dbName, ArrayNode node);

    public void insert(String dbName, String collectionName, String jsonContent);

    public boolean shutdown();

    public void cleanDb(String dbName);

    public void cleanAllDbs();

    public void insert(String dbName, String collectionName, ArrayNode jsonArray);
}

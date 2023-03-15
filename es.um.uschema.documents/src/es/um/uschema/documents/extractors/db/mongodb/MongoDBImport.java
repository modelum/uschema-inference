package es.um.uschema.documents.extractors.db.mongodb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import es.um.uschema.documents.extractors.util.MapReduceSources;
import es.um.uschema.documents.extractors.util.MongoDBStreamAdapter;

public class MongoDBImport
{
    private MongoDBStreamAdapter adapter;
    private String dbIP;
    private String tableName;

    public MongoDBImport(String dbIP, String tableName)
    {
        this.adapter = new MongoDBStreamAdapter();
        this.dbIP = dbIP;
        this.tableName = tableName;
    }

    public Stream<JsonObject> mapRed2Stream(Path mapRedDir)
    {
        return mapRed2Stream(new MapReduceSources(mapRedDir));
    }

    public Stream<JsonObject> mapRed2Stream(MapReduceSources mrs)
    {
        return performMapReduce(mrs);
    }

    public JsonArray mapRed2Array(Path mapRedDir)
    {
        return mapRed2Array(new MapReduceSources(mapRedDir));
    }

    public JsonArray mapRed2Array(MapReduceSources mrs)
    {
        return adapter.stream2JsonArray(performMapReduce(mrs));
    }

    public void mapRed2File(Path mapReduceSources, File outputFile)
    {
        mapRed2File(new MapReduceSources(mapReduceSources), outputFile);
    }

    public void mapRed2File(MapReduceSources mrs, File outputFile)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try
        {
            PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
            writer.print(gson.toJson(adapter.stream2JsonObject(performMapReduce(mrs))));
            writer.close();
        } catch (IOException e)
        {
            System.err.println("Error while writing JSON inference to file!");
        }
    }

    private Stream<JsonObject> performMapReduce(MapReduceSources mrs)
    {
        MongoClient mClient = MongoClients.create("mongodb://" + dbIP + ":27017");

        MongoDatabase database = mClient.getDatabase(tableName);
        Map<String, MapReduceIterable<Document>> mapRedMap = new HashMap<String, MapReduceIterable<Document>>();

        for (String collName : database.listCollectionNames())
        {
            MongoCollection<Document> collection = database.getCollection(collName);
            mapRedMap.put(collName, collection.mapReduce(mrs.getMapJSCode(), mrs.getReduceJSCode()));
        }

//    String map = "function map() { emit (null, 1) ; }";
//    String reduce = "function reduce(k, vals) { return Array.sum(vals);}";
//
//    MongoCollection<Document> collection = database.getCollection("movie");
//    MapReduceIterable<Document> result = collection.mapReduce(map, reduce);
//
//    for(Document d : result )
//    {
//    	System.out.println(d);
//    }
//
//    mClient.close();
        Stream<JsonObject> result = adapter.adaptStream(mapRedMap);
        result = result.onClose(() -> {
            mClient.close();
        });

        return result;
    }
}

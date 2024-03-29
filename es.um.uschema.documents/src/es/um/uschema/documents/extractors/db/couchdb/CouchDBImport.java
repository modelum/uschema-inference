package es.um.uschema.documents.extractors.db.couchdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.DesignDocument.MapReduce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import es.um.uschema.documents.extractors.util.CouchDBStreamAdapter;
import es.um.uschema.documents.extractors.util.MapReduceSources;

public class CouchDBImport
{
    private CouchDBStreamAdapter adapter;
    private String dbIP;
    private String tableName;

    public CouchDBImport(String dbIP, String tableName)
    {
        this.adapter = new CouchDBStreamAdapter();
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

    public void mapRed2File(Path mapRedDir, File outputFile)
    {
        mapRed2File(new MapReduceSources(mapRedDir), outputFile);
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
        CouchDbProperties properties = new CouchDbProperties(tableName.toLowerCase(), true, "http", dbIP, 5984, null,
                null);
        CouchDbClient dbClient = new CouchDbClient(properties);
        MapReduce mapRedObj = new MapReduce();
        mapRedObj.setMap(mrs.getMapJSCode());
        mapRedObj.setReduce(mrs.getReduceJSCode());
        List<JsonObject> list = dbClient.view("_temp_view").tempView(mapRedObj).group(true)
                .includeDocs(false).reduce(true).query(JsonObject.class);

        // This step will apply some custom rules to the elements...
        return adapter.adaptStream(list);
    }
}

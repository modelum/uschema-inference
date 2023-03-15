package es.um.uschema.documents.main.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.JsonArray;

import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.couchdb.CouchDBImport;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.Source2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.main.util.constants.ConfigConstants;

public class FillInferDb
{
    private DbType dbType;

    public FillInferDb(DbType dbType)
    {
        this.dbType = dbType;
    }

    public void fillDbFromFile(Source2Db controller, String dbName, File sourceFile)
    {
        long startTime = System.currentTimeMillis();

        if (ConfigConstants.DEBUG)
            System.out.println("Filling the " + dbType.toString() + " database...");

        controller.run(sourceFile, dbName);
        controller.shutdown();

        if (ConfigConstants.DEBUG)
            System.out.println("Database " + dbName + " filled in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void fillDbFromFolder(Source2Db controller, String dbName, File sourceFolder)
    {
        long startTime = System.currentTimeMillis();

        if (ConfigConstants.DEBUG)
            System.out.println("Filling the " + dbType.toString() + " database...");

        for (String fileName : sourceFolder.list())
            if (fileName.endsWith(".xml") || fileName.endsWith(".csv"))
                controller.run(sourceFolder.toPath().resolve(fileName).toFile(), dbName);

        controller.shutdown();

        if (ConfigConstants.DEBUG)
            System.out.println("Database " + dbName + " filled in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void inferFromDb(String dbName, File outputModel)
    {
        long startTime = System.currentTimeMillis();

        if (ConfigConstants.DEBUG)
            System.out.println("Starting inference...");

        JsonArray jArray = null;

        switch (dbType)
        {
        case MONGODB:
        {
            MongoDBImport inferrer = new MongoDBImport(ConfigConstants.DATABASE_IP, dbName);
            jArray = inferrer.mapRed2Array(Paths.get(ConfigConstants.MONGODB_MAPREDUCE_FOLDER));
            break;
        }
        case COUCHDB:
        {
            CouchDBImport inferrer = new CouchDBImport(ConfigConstants.DATABASE_IP, dbName);
            jArray = inferrer.mapRed2Array(Paths.get(ConfigConstants.COUCHDB_MAPREDUCE_FOLDER));
            break;
        }
        }

        long mapReduceTime = System.currentTimeMillis();

        if (ConfigConstants.DEBUG)
        {
            System.out.println("Inference finished.");
            System.out.println("Starting BuildUSchema...");
        }
        // Gson gson = new GsonBuilder().setPrettyPrinting().create();

        BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
        builder.buildFromGsonArray(dbName, jArray);

        if (ConfigConstants.DEBUG)
            System.out.println("BuildUSchema created: " + outputModel + " in "
                    + (System.currentTimeMillis() - startTime) + " ms (mapReduce time: "
                    + (mapReduceTime - startTime) + " ms)");

        try
        {
            Files.createDirectories(outputModel.getParentFile().toPath());
            builder.writeToFile(outputModel);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

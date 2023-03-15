package test;

import java.io.File;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.Companies2Db;
import es.um.uschema.documents.injectors.util.DbType;

public class CompaniesTest
{
    private static final String DATABASE_IP = "localhost";
    private static final String MONGODB_MAPREDUCE_FOLDER = "mapreduce/mongodb/v1";
    private static String INPUT_FILE = "testSources/ERROR_Companies.json";
    private static String DBNAME = "DEBUG_Companies";
    private static String OUTPUT_MODEL = "testOutput/" + DBNAME + ".xmi";

    private Companies2Db controller;

    @Before
    public void setUp() throws Exception
    {
        controller = new Companies2Db(DbType.MONGODB, DATABASE_IP);
    }

    @After
    public void tearDown() throws Exception
    {
        controller.getClient().cleanDb(DBNAME);
        controller.shutdown();
    }

    @Test
    public void test()
    {
        controller.run(new File(INPUT_FILE), DBNAME);

        System.out.println("Starting inference...");
        MongoDBImport inferrer = new MongoDBImport(DATABASE_IP, DBNAME);
        JsonArray jArray = inferrer.mapRed2Array(Paths.get(MONGODB_MAPREDUCE_FOLDER));
        System.out.println("Inference finished.");

        System.out.println("Starting BuildUSchema...");
        BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
        builder.buildFromGsonArray(DBNAME, jArray);
        builder.writeToFile(new File(OUTPUT_MODEL));

        System.out.println("BuildUSchema created: " + OUTPUT_MODEL);
        // TODO: Actually fail on exception...
        // TODO: Check model integrity
        // TODO: Another NULL value it seems. Have to check in more detail.
    }
}

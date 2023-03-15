package test;

import java.io.File;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.Webclick2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.utils.USchemaSerializer;

public class MapReduceTimestampTest
{
  private String inputRoute = "testSources/MapReduceTimestamp.json";
  private String dbName = "DEBUG_MapReduceTimestamp";
  private Webclick2Db controller;

  @Before
  public void setUp() throws Exception
  {
    controller = new Webclick2Db(DbType.MONGODB, "localhost");
  }

  @After
  public void tearDown() throws Exception
  {
    controller.getClient().cleanDb(dbName);
    controller.shutdown();
  }

  @Test
  public void test()
  {
    controller.run(new File(inputRoute), dbName);

    MongoDBImport inferrer = new MongoDBImport("localhost", dbName);
    JsonArray jArray = inferrer.mapRed2Array(Path.of("mapreduce/mongodb/v2/"));

    BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
    builder.buildFromGsonArray(dbName, jArray);
    USchema schema = builder.getUSchema();

    USchemaSerializer serializer = new USchemaSerializer();
    System.out.println(serializer.serialize(schema));
  }
}

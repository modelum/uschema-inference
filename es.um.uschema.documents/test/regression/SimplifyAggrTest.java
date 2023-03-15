package regression;

import java.io.File;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.EveryPolitician2Db;
import es.um.uschema.documents.injectors.util.DbType;

/**
 * Validation test: The inference process should be able to simplify Aggr{V1, V2, V2,...V2} to Aggr{V1, V2}.
 * If this fails, the inferrer is aggregating variations which already are added to the list.
 * @fail: Several variations are stored as different when in fact they are equivalent.
 */
public class SimplifyAggrTest
{
  private String inputRoute = "testSources/SimplifyAggr.json";
  private String dbName = "DEBUG_SimplifyAggr";
  private EveryPolitician2Db controller;

  @Before
  public void setUp() throws Exception
  {
    controller = new EveryPolitician2Db(DbType.MONGODB, "localhost");
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
    JsonArray jArray = inferrer.mapRed2Array(Path.of("mapreduce/mongodb/v1/"));

    BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
    builder.buildFromGsonArray(dbName, jArray);
    USchema uSchema = builder.getUSchema();

    Assert.assertEquals(2, uSchema.getEntities().size());
    Assert.assertEquals(2, uSchema.getEntities().get(0).getVariations().size());
    Assert.assertEquals(2, uSchema.getEntities().get(1).getVariations().size());
  }
}
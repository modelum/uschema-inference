package regression;

import java.io.File;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.EveryPolitician2Db;
import es.um.uschema.documents.injectors.util.DbType;

/**
 * Validation test: The inference process should be able to infer correctly the count and timestamp of root entities variations.
 * In non root entity variations, the count and timestamp is copied from the parents, for now.
 * @fail: Count and timestamp are not correctly calculated.
 */
public class CountTimestampTest
{
  private String inputRoute = "testSources/CountTimestamp.json";
  private String dbName = "DEBUG_CountTimestamp";
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

    Assert.assertEquals(3, uSchema.getEntities().size());
    EntityType entity = uSchema.getEntities().get(0);
    Assert.assertEquals(2, entity.getVariations().size());
    Assert.assertEquals(1, entity.getVariations().get(0).getVariationId());
    Assert.assertEquals(1, entity.getVariations().get(0).getCount());
    Assert.assertEquals(0, entity.getVariations().get(0).getFirstTimestamp());
    Assert.assertEquals(0, entity.getVariations().get(0).getLastTimestamp());
    Assert.assertEquals(2, entity.getVariations().get(1).getVariationId());
    Assert.assertEquals(1, entity.getVariations().get(1).getCount());
    Assert.assertEquals(0, entity.getVariations().get(1).getFirstTimestamp());
    Assert.assertEquals(0, entity.getVariations().get(1).getLastTimestamp());

    entity = uSchema.getEntities().get(1);
    Assert.assertEquals(1, entity.getVariations().size());
    Assert.assertEquals(1, entity.getVariations().get(0).getVariationId());
    Assert.assertEquals(2, entity.getVariations().get(0).getCount());
    Assert.assertEquals(0, entity.getVariations().get(0).getFirstTimestamp());
    Assert.assertEquals(0, entity.getVariations().get(0).getLastTimestamp());

    entity = uSchema.getEntities().get(2);
    Assert.assertEquals(2, entity.getVariations().size());
    Assert.assertEquals(1, entity.getVariations().get(0).getVariationId());
    Assert.assertEquals(8, entity.getVariations().get(0).getCount());
    Assert.assertEquals(0, entity.getVariations().get(0).getFirstTimestamp());
    Assert.assertEquals(0, entity.getVariations().get(0).getLastTimestamp());
    Assert.assertEquals(2, entity.getVariations().get(1).getVariationId());
    Assert.assertEquals(3, entity.getVariations().get(1).getCount());
    Assert.assertEquals(0, entity.getVariations().get(1).getFirstTimestamp());
    Assert.assertEquals(0, entity.getVariations().get(1).getLastTimestamp());
  }
}

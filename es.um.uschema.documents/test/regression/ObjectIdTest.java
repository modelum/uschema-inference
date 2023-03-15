package regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.EveryPolitician2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Feature;

/**
 * Validation test: The inference process should be able to differentiate between strings and ObjectIds
 * in attributes as well as in the original types of references. If this fails, please check the inference
 * process and also the mapReduce files.
 * @fail: An ObjectId is inferred as a String or as an Aggregated entity.
 */
public class ObjectIdTest
{
  private String inputRoute = "testSources/ObjectIds.json";
  private String dbName = "DEBUG_ObjectIds";
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

    assertNotNull("Schema can't be null", uSchema);
    assertNotNull("Schema should have entities", uSchema.getEntities());
    assertEquals("Schema should have one entity", 1, uSchema.getEntities().size());

    Feature feat = uSchema.getEntities().get(0).getVariations().get(0).getFeatures().stream().filter(p -> p.getName().equals("_id")).findFirst().get();

    assertTrue(feat instanceof Attribute);
    assertEquals("ObjectId", ((PrimitiveType)((Attribute)feat).getType()).getName());
  }
}

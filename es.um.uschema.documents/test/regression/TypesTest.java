package regression;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.EveryPolitician2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.USchema.Feature;

/**
 * Validation test: The inference process should be able to use an internal "_type" Attribute
 * which in the end doesn't show on the model.
 * @fail: A _type attribute exists.
 */
public class TypesTest
{
  private String inputRoute = "testSources/Types.json";
  private String dbName = "DEBUG_Type";
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

    for (EntityType e : uSchema.getEntities())
      for (StructuralVariation ev : e.getVariations())
      {
        Optional<Feature> prop = ev.getFeatures().stream().filter(p -> p.getName().equals("_type")).findFirst();
        Assert.assertFalse(prop.isPresent());
      }
  }
}

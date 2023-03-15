package test;

import static org.junit.Assert.*;

import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Key;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.injectors.interfaces.Json2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.utils.USchemaSerializer;

public class UserProfileTest
{
  private static String dbName = "userprofile";
  private static Json2Db controller;

  @Before
  public void setUp() throws Exception
  {
    controller = new Json2Db(DbType.MONGODB, "localhost");
  }

  @After
  public void tearDown() throws Exception
  {
    controller.shutdown();
  }

  @Test
  public void test()
  {
    MongoDBImport inferrer = new MongoDBImport("localhost", dbName);
    JsonArray jArray = inferrer.mapRed2Array(Path.of("mapreduce/mongodb/v1/"));

    BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
    builder.buildFromGsonArray(dbName, jArray);
    USchema schema = builder.getUSchema();

    EntityType movie = schema.getEntities().get(0);
    assertTrue(movie.getVariations().get(0).getFeatures().stream().filter(f -> f instanceof Key).count() == 1);

    EntityType user = schema.getEntities().get(1);
    assertTrue(user.getVariations().get(0).getFeatures().stream().filter(f -> f instanceof Key).count() == 1);
    assertTrue(user.getVariations().get(1).getFeatures().stream().filter(f -> f instanceof Key).count() == 1);

    assertFalse(((Aggregate)user.getVariations().get(0).getFeatures().stream().filter(f -> f instanceof Aggregate && ((Aggregate)f).getName().equals("address")).findAny().get()).isOptional());
    assertFalse(((Aggregate)user.getVariations().get(1).getFeatures().stream().filter(f -> f instanceof Aggregate && ((Aggregate)f).getName().equals("address")).findAny().get()).isOptional());

    USchemaSerializer serializer = new USchemaSerializer();
    System.out.println(serializer.serialize(schema));
  }
}

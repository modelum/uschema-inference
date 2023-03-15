package es.um.uschema.documents.injectors.interfaces;

import java.io.File;

import com.fasterxml.jackson.databind.node.ArrayNode;

import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.injectors.util.generator.JsonGenerator;

public class Model2Db extends Source2Db
{
  public Model2Db(DbType db, String ip)
  {
    super(db, ip);
  }

  public void run(File modelRoute, String dbName)
  {
    int minInstances = 1000;
    int maxInstances = 1000;

    this.run(modelRoute, dbName, minInstances, maxInstances);
  }

  public void run(File modelRoute, String dbName, int minInstances, int maxInstances)
  {
    long startTime = System.currentTimeMillis();

    System.out.println("Reading input model " + modelRoute + "...");
    storeJSONContent(modelRoute, dbName, minInstances, maxInstances);
    System.out.println(modelRoute.getName() + " table created in " + (System.currentTimeMillis() - startTime) + " ms");
  }

  private void storeJSONContent(File modelRoute, String dbName, int minInstances, int maxInstances)
  {
    EcoreModelIO loader = new EcoreModelIO();
    JsonGenerator generator = new JsonGenerator();

    USchema schema = loader.load(USchema.class, modelRoute.toPath());

    ArrayNode jsonContent = null;
	try
    {
      jsonContent  = generator.generate(schema, minInstances, maxInstances);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    getClient().cleanDb(schema.getName().toLowerCase());
    getClient().insert(dbName, jsonContent);
  }
}

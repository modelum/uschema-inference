package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultJsonConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.JsonTest;

public class JsonMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultJsonConfig.config());

        JsonTest Json = new JsonTest();
        Json.runTest(exampleConf);

        System.exit(0);
    }
}

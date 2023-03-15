package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultEveryPoliticianConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.EveryPoliticianTest;

public class EveryPoliticianMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultEveryPoliticianConfig.config());

        EveryPoliticianTest EveryPolitician = new EveryPoliticianTest();
        EveryPolitician.runTest(exampleConf);

	System.exit(0);
    }
}

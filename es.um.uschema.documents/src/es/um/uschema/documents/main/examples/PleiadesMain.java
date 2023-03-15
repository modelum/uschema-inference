package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultPleiadesConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.PleiadesTest;

public class PleiadesMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultPleiadesConfig.config());

        PleiadesTest Pleiades = new PleiadesTest();
        Pleiades.runTest(exampleConf);

	System.exit(0);
    }
}

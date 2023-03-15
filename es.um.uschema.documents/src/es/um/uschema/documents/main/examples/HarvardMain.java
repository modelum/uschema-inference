package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultHarvardConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.HarvardTest;

public class HarvardMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultHarvardConfig.config());

        HarvardTest Harvard = new HarvardTest();
        Harvard.runTest(exampleConf);

	System.exit(0);
    }
}

package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultStackOverflowConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.StackOverflowTest;

public class StackOverflowMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultStackOverflowConfig.config());

        StackOverflowTest StackOverflow = new StackOverflowTest();
        StackOverflow.runTest(exampleConf);

	System.exit(0);
    }
}

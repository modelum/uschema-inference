package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultModelConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.ModelTest;

public class ModelMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultModelConfig.config());

        ModelTest Model = new ModelTest();
        Model.runTest(exampleConf);

	System.exit(0);
    }
}

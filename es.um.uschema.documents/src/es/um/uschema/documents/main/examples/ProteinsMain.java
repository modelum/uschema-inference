package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultProteinsConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.ProteinsTest;

public class ProteinsMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultProteinsConfig.config());

        ProteinsTest Proteins = new ProteinsTest();
        Proteins.runTest(exampleConf);

	System.exit(0);
    }
}

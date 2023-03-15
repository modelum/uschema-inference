package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultPublicationsConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.PublicationsTest;

public class PublicationsMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultPublicationsConfig.config());

        PublicationsTest Publications = new PublicationsTest();
        Publications.runTest(exampleConf);

	System.exit(0);
    }
}

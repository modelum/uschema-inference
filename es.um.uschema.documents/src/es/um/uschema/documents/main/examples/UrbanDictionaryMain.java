package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultUrbanDictionaryConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.UrbanDictionaryTest;

public class UrbanDictionaryMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultUrbanDictionaryConfig.config());

        UrbanDictionaryTest UrbanDictionary = new UrbanDictionaryTest();
        UrbanDictionary.runTest(exampleConf);

	System.exit(0);
    }
}

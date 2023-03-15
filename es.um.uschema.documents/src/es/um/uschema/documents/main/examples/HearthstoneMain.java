package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultHearthstoneConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.HearthstoneTest;

public class HearthstoneMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultHearthstoneConfig.config());

        HearthstoneTest Hearthstone = new HearthstoneTest();
        Hearthstone.runTest(exampleConf);

	System.exit(0);
    }
}

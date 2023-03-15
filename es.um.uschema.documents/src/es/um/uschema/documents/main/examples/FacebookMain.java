package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultFacebookConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.FacebookTest;

public class FacebookMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultFacebookConfig.config());

        FacebookTest Facebook = new FacebookTest();
        Facebook.runTest(exampleConf);

	System.exit(0);
    }
}

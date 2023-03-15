package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultRedditConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.RedditTest;

public class RedditMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultRedditConfig.config());

        RedditTest Reddit = new RedditTest();
        Reddit.runTest(exampleConf);

	System.exit(0);
    }
}

package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultLinksConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.LinksTest;

public class LinksMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultLinksConfig.config());

        LinksTest Links = new LinksTest();
        Links.runTest(exampleConf);

	System.exit(0);
    }
}

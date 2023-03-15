package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultWebclickConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.WebclickTest;

public class WebclickMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultWebclickConfig.config());

        WebclickTest Webclick = new WebclickTest();
        Webclick.runTest(exampleConf);

	System.exit(0);
    }
}

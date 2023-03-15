package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultOpenSanctionsConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.OpenSanctionsTest;

public class OpenSanctionsMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultOpenSanctionsConfig.config());

        OpenSanctionsTest OpenSanctions = new OpenSanctionsTest();
        OpenSanctions.runTest(exampleConf);

	System.exit(0);
    }
}

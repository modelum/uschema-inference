package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultCompaniesConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.CompaniesTest;

public class CompaniesMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultCompaniesConfig.config());

        CompaniesTest Companies = new CompaniesTest();
        Companies.runTest(exampleConf);

	System.exit(0);
    }
}

package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultSolarSystemConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.SolarSystemTest;

public class SolarSystemMain
{
    public static void main(String[] args)
    {
        // Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultSolarSystemConfig.config());

        SolarSystemTest SolarSystem = new SolarSystemTest();
        SolarSystem.runTest(exampleConf);

	System.exit(0);
    }
}

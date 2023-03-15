package es.um.uschema.documents.main.examples;

import es.um.uschema.documents.main.examples.config.DefaultUserProfileConfig;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;
import es.um.uschema.documents.main.examples.tests.UserProfileTest;

public class UserProfileMain
{
    public static void main(String[] args)
    {
        ExampleConfigurationObject exampleConf = new ExampleConfigurationObject();
        exampleConf.processRunArgs(args);
        exampleConf.mergeWithDefaults(DefaultUserProfileConfig.config());

        if (args.length > 0)
        {
            exampleConf.put("db.ip", args[0]);
        }

        if (args.length > 1)
        {
            exampleConf.put("db.name", args[1]);
        }

        if (args.length > 2)
        {
            exampleConf.put("outputfile", args[2]);
        }

        UserProfileTest userProfileTest = new UserProfileTest();
        userProfileTest.runTest(exampleConf);

        System.exit(0);
    }
}

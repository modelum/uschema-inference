package es.um.uschema.documents.main.examples.tests;

import java.io.File;
import java.net.URISyntaxException;

import com.google.gson.JsonArray;

import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.DefaultBuildUSchema;
import es.um.uschema.documents.extractors.db.mongodb.MongoDBImport;
import es.um.uschema.documents.extractors.util.PathUtils;
import es.um.uschema.documents.main.examples.config.ExampleConfigurationObject;

public class UserProfileTest
{
    public void runTest(ExampleConfigurationObject exampleConf)
    {
        MongoDBImport inferrer = new MongoDBImport((String) exampleConf.get("db.ip"),
                (String) exampleConf.get("db.name"));

        JsonArray jArray = null;
        try
        {
            jArray = inferrer
                    .mapRed2Array(PathUtils.get(ClassLoader.getSystemResource("mapreduce/mongodb/v1/").toURI()));
        } catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
        builder.buildFromGsonArray((String) exampleConf.get("db.name"), jArray);
        // USchema schema = builder.getUSchema();
        builder.writeToFile(new File((String) exampleConf.getOrDefault("outputfile", "output/output.xmi")));
    }

}

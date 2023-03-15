package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class DefaultUserProfileConfig
{
    static Properties props = new Properties();
    static
    {
        props.put("db.ip", "localhost");
        // props.load(file);
    }

    public static Properties config()
    {
        return props;
    }

}

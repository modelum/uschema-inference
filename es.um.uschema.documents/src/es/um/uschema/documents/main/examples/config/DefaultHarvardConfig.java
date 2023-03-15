package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class DefaultHarvardConfig
{
    static Properties props = new Properties();
    static
    {
        props.put("Harvard.source", "file");
        // props.load(file);
    }

    public static Properties config()
    {
        return props;
    }

}

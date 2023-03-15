package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class DefaultSolarSystemConfig
{
    static Properties props = new Properties();
    static
    {
        props.put("SolarSystem.source", "file");
        // props.load(file);
    }

    public static Properties config()
    {
        return props;
    }

}

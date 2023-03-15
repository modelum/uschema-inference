package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class DefaultProteinsConfig
{
    static Properties props = new Properties();
    static
    {
        props.put("Proteins.source", "file");
        // props.load(file);
    }

    public static Properties config()
    {
        return props;
    }

}

package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class DefaultWebclickConfig
{
    static Properties props = new Properties();
    static
    {
        props.put("Webclick.source", "file");
        // props.load(file);
    }

    public static Properties config()
    {
        return props;
    }

}

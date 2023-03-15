package es.um.uschema.documents.main.examples.config;

import java.util.Properties;

public class ExampleConfigurationObject
{
    private Properties config = new Properties();

    public void processRunArgs(String[] args)
    {
        // TODO Auto-generated method stub

    }

    public void mergeWithDefaults(Properties config)
    {
        // TODO correctly merge
        config.entrySet().stream().forEach(e -> this.config.put(e.getKey(), e.getValue()));
    }

    public Properties getProperties()
    {
        return config;
    }

    public Object get(Object key)
    {
        return config.get(key);
    }

    public Object getOrDefault(Object key, Object defaultValue)
    {
        return config.getOrDefault(key, defaultValue);
    }

    public ExampleConfigurationObject put(Object key, Object value)
    {
        config.put(key, value);
        return this;
    }
}

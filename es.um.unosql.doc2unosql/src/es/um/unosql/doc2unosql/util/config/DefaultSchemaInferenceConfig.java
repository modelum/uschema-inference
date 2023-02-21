package es.um.unosql.doc2unosql.util.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultSchemaInferenceConfig implements SchemaInferenceConfig
{
  private static final Set<String> IGNORED_ATTRIBUTES = new HashSet<>(Arrays.asList("_type"));

  @Override
  public Set<String> getIgnoredAttributes()
  {
    return IGNORED_ATTRIBUTES;
  }

  @Override
  public String getTypeMarkerAttribute()
  {
    return "_type";
  }
}

package es.um.uschema.doc2uschema.util.config;

import java.util.Set;

public interface SchemaInferenceConfig 
{
  Set<String> getIgnoredAttributes();

  String getTypeMarkerAttribute();
}
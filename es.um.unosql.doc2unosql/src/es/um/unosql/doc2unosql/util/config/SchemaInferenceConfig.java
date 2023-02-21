package es.um.unosql.doc2unosql.util.config;

import java.util.Set;

public interface SchemaInferenceConfig 
{
  Set<String> getIgnoredAttributes();

  String getTypeMarkerAttribute();
}
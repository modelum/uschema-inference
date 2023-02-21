package es.um.unosql.doc2unosql.process.util;

import java.util.Map.Entry;
import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;

/**
 * @author dsevilla
 *
 */
public interface OptionalTagger 
{
  void put(String entityTypeName, SchemaComponent schema);

  // Main call method
  void calcOptionality();

  // Is optional this component in this entityName?
  boolean isOptional(String entityName, Entry<String, SchemaComponent> sc);
}

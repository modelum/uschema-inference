/**
 *
 */
package es.um.uschema.doc2uschema.process.util;

import java.util.Map;

import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;

/**
 * @author dsevilla
 *
 */
public class NullOptionalTagger implements OptionalTagger
{
	public NullOptionalTagger()
	{
	}

	@Override
	public void put(String entityTypeName, SchemaComponent schema) {
	}

	@Override
	public void calcOptionality()
	{
	}

	@Override
	public boolean isOptional(String entityName, Map.Entry<String,SchemaComponent> sc)
	{
		return false;
	}
}

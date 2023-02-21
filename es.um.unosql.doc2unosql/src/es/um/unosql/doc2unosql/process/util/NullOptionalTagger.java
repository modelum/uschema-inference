/**
 *
 */
package es.um.unosql.doc2unosql.process.util;

import java.util.Map;

import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;

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

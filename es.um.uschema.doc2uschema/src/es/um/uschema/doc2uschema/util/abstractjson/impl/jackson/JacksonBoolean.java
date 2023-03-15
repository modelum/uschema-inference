/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJBoolean;

/**
 * @author dsevilla
 *
 */
public class JacksonBoolean extends JacksonElement implements IAJBoolean
{
	public JacksonBoolean(JsonNode val) {
		super(val);
	}
}

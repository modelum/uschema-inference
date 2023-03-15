/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJTextual;

/**
 * @author dsevilla
 *
 */
public class JacksonTextual extends JacksonElement implements IAJTextual
{
	public JacksonTextual(JsonNode val) {
		super(val);
	}
}

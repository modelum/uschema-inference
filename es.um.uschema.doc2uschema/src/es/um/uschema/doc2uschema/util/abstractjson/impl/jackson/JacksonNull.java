/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJNull;

/**
 * @author dsevilla
 *
 */
public class JacksonNull extends JacksonElement implements IAJNull
{
	public JacksonNull(JsonNode val) {
		super(val);
	}
}

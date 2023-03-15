/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJObject;

/**
 * @author dsevilla
 *
 */
public class JacksonObject extends JacksonElement implements IAJObject
{
	public JacksonObject(JsonNode val) {
		super(val);
	}
}

/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJNumber;

/**
 * @author dsevilla
 *
 */
public class JacksonNumber extends JacksonElement implements IAJNumber
{
	public JacksonNumber(JsonNode val) {
		super(val);
	}
}

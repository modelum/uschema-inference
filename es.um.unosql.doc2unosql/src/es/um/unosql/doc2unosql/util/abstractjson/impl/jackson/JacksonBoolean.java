/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJBoolean;

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

/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJTextual;

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

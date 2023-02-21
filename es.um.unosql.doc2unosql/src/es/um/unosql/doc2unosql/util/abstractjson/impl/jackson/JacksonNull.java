/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJNull;

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

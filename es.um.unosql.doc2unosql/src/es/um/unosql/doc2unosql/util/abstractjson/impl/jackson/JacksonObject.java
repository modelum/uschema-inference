/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJObject;

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

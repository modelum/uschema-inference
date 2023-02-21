/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJObjectId;

public class JacksonObjectId extends JacksonElement implements IAJObjectId
{
	public JacksonObjectId(JsonNode val) {
		super(val);
	}
}

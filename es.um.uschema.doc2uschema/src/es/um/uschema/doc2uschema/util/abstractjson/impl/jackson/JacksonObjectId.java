/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.util.abstractjson.IAJObjectId;

public class JacksonObjectId extends JacksonElement implements IAJObjectId
{
	public JacksonObjectId(JsonNode val) {
		super(val);
	}
}

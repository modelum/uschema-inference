package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJObjectId;

public class GsonObjectId extends GsonElement implements IAJObjectId
{
	public GsonObjectId(JsonElement val) {
		super(val);
	}
}

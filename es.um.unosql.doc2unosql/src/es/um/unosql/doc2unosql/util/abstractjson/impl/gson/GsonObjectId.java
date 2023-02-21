package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJObjectId;

public class GsonObjectId extends GsonElement implements IAJObjectId
{
	public GsonObjectId(JsonElement val) {
		super(val);
	}
}

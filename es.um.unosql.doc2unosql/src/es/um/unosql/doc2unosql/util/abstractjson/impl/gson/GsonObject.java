package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJObject;

/**
 * @author dsevilla
 *
 */
public class GsonObject extends GsonElement implements IAJObject
{
	public GsonObject(JsonElement val) {
		super(val);
	}
}

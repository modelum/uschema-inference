package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJObject;

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

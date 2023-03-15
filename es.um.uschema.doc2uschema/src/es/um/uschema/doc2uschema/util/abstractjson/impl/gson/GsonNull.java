package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJNull;

/**
 * @author dsevilla
 *
 */
public class GsonNull extends GsonElement implements IAJNull
{
	public GsonNull(JsonElement val) {
		super(val);
	}
}

package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJTextual;

/**
 * @author dsevilla
 *
 */
public class GsonTextual extends GsonElement implements IAJTextual
{
	public GsonTextual(JsonElement val) {
		super(val);
	}
}

package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJNumber;

/**
 * @author dsevilla
 *
 */
public class GsonNumber extends GsonElement implements IAJNumber
{
	public GsonNumber(JsonElement val) {
		super(val);
	}
}

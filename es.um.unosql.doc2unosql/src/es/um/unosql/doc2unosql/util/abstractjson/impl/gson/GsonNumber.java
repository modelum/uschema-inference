package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJNumber;

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

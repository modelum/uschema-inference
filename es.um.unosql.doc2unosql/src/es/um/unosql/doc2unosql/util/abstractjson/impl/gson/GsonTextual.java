package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJTextual;

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

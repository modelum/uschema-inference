package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;
import es.um.unosql.doc2unosql.util.abstractjson.IAJNull;

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

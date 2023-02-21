/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJArray;

/**
 * @author dsevilla
 *
 */
public class GsonArray extends GsonElement implements IAJArray
{
	public GsonArray(JsonElement val) {
		super(val);
	}
}

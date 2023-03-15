/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJArray;

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

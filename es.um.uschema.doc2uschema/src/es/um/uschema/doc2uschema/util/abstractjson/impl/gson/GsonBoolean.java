/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.uschema.doc2uschema.util.abstractjson.IAJBoolean;

/**
 * @author dsevilla
 *
 */
public class GsonBoolean extends GsonElement implements IAJBoolean
{
	public GsonBoolean(JsonElement val) {
		super(val);
	}
}

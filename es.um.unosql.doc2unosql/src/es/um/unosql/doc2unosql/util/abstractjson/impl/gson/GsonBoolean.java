/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.gson;

import com.google.gson.JsonElement;

import es.um.unosql.doc2unosql.util.abstractjson.IAJBoolean;

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

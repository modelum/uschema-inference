/**
 *
 */
package es.um.uschema.doc2uschema.util.abstractjson;

import java.io.File;

import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;

/**
 * @author dsevilla
 *
 */
public interface IAJAdapter<JET>
{
	public IAJElement wrap(JET e);

	public IAJElement readFromFile(File jsonFile) throws JSONException;
}

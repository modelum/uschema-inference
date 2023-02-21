/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson;

import java.io.File;

import es.um.unosql.doc2unosql.util.abstractjson.except.JSONException;

/**
 * @author dsevilla
 *
 */
public interface IAJAdapter<JET>
{
	public IAJElement wrap(JET e);

	public IAJElement readFromFile(File jsonFile) throws JSONException;
}

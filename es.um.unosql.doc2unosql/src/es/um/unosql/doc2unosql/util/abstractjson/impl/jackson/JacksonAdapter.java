/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.um.unosql.doc2unosql.util.abstractjson.IAJAdapter;
import es.um.unosql.doc2unosql.util.abstractjson.IAJElement;
import es.um.unosql.doc2unosql.util.abstractjson.except.JSONException;

/**
 * @author dsevilla
 *
 */
public class JacksonAdapter implements IAJAdapter<JsonNode>
{
    public static IAJElement _wrap(JsonNode e)
    {
        if (e == null)
            return null;
        return new JacksonElement(e);
    }

    @Override
    public IAJElement wrap(JsonNode e)
    {
        return _wrap(e);
    }

    @Override
    public IAJElement readFromFile(File jsonFile) throws JSONException
    {
        ObjectMapper m = new ObjectMapper();
        try
        {
            JsonNode root = m.readTree(jsonFile);
            return wrap(root);
        } catch (Exception e)
        {
            throw new JSONException(e.getMessage());
        }
    }
}

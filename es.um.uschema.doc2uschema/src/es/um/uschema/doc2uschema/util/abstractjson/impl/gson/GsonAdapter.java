package es.um.uschema.doc2uschema.util.abstractjson.impl.gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.um.uschema.doc2uschema.util.abstractjson.IAJAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.IAJElement;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;

/**
 * @author dsevilla
 *
 */
public class GsonAdapter implements IAJAdapter<JsonElement>
{
    public static IAJElement _wrap(JsonElement e)
    {
        if (e == null)
            return null;
        return new GsonElement(e);
    }

    @Override
    public IAJElement wrap(JsonElement e)
    {
        return _wrap(e);
    }

    @Override
    public IAJElement readFromFile(File jsonFile) throws JSONException
    {
        try
        {
            JsonObject root = JsonParser.parseReader(new BufferedReader(new FileReader(jsonFile))).getAsJsonObject();
            return wrap(root);
        } catch (Exception e)
        {
            throw new JSONException(e.getMessage());
        }
    }
}

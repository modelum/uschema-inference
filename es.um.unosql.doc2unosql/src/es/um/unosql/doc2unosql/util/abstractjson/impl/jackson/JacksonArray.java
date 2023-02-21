/**
 *
 */
package es.um.unosql.doc2unosql.util.abstractjson.impl.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.unosql.doc2unosql.util.abstractjson.IAJArray;

/**
 * @author dsevilla
 *
 */
public class JacksonArray extends JacksonElement implements IAJArray
{
    public JacksonArray(JsonNode val)
    {
        super(val);
    }
}

/**
 *
 */
package es.um.unosql.doc2unosql.main.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author dsevilla
 *
 */
public class JSON2RawSchema
{
    private static ObjectMapper m = new ObjectMapper();

    public static JsonNode fromJSON(String json) throws JsonProcessingException, IOException
    {
        return m.readTree(json);
    }
}

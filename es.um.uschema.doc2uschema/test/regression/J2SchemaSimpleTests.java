package regression;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;
import es.um.uschema.doc2uschema.intermediate.raw.util.SchemaPrinter;
import es.um.uschema.doc2uschema.main.util.JSON2RawSchema;
import es.um.uschema.doc2uschema.main.util.RawSchemaGen;

public class J2SchemaSimpleTests
{
    @Test
    public void test1()
    {
        try
        {
            JsonNode n = JSON2RawSchema.fromJSON("{\"a\": 1}");

            SchemaComponent sc = RawSchemaGen.deSchema(null, n);
            String result = SchemaPrinter.schemaString(sc);

            assertEquals("test1", "<null>{\"a\": Number } ", result);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void test2()
    {
        try
        {
            JsonNode n = JSON2RawSchema.fromJSON("{\"a\": [1,2,3]}");

            SchemaComponent sc = RawSchemaGen.deSchema(null, n);
            String result = SchemaPrinter.schemaString(sc);

            assertEquals("test2", "<null>{\"a\": [Number ] } ", result);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void test3()
    {
        try
        {
            JsonNode n = JSON2RawSchema.fromJSON("{\"c\": 1,\"b\": 1,\"a\": 1}");

            SchemaComponent sc = RawSchemaGen.deSchema(null, n);
            String result = SchemaPrinter.schemaString(sc);

            assertEquals("test3", "<null>{\"a\": Number \"b\": Number \"c\": Number } ", result);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

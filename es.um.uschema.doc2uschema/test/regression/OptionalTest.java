package regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.main.util.JSON2RawSchema;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonArray;
import regression.config.OptionalTestConfig;

class OptionalTest
{
    private String jsonContent = "[{\r\n" +
            "  \"schema\": {\r\n" +
            "    \"_id\": \"s\",\r\n" +
            "    \"optionalAttr\": \"s\",\r\n" +
            "    \"requiredAttr\": \"s\",\r\n" +
            "    \"_type\": \"MyEntity\"\r\n" +
            "  },\r\n" +
            "  \"count\": 1,\r\n" +
            "  \"firstTimestamp\": 0,\r\n" +
            "  \"lastTimestamp\": 0\r\n" +
            "}, {\r\n" +
            "  \"schema\": {\r\n" +
            "    \"_id\": \"s\",\r\n" +
            "    \"requiredAttr\": \"s\",\r\n" +
            "    \"_type\": \"MyEntity\"\r\n" +
            "  },\r\n" +
            "  \"count\": 10,\r\n" +
            "  \"firstTimestamp\": 0,\r\n" +
            "  \"lastTimestamp\": 0\r\n" +
            "}]";

    private BuildUSchema builder;

    @BeforeEach
    public void setUp()
    {
        builder = new OptionalTestConfig().getUSchema();
    }

    @Test
    public void test() throws JSONException, IOException
    {
        JsonNode content = JSON2RawSchema.fromJSON(jsonContent);

        builder.buildFromArray("test", new JacksonArray(content), new JacksonAdapter());
        USchema schema = builder.getUSchema();

        assertEquals(schema.getEntities().size(), 1);

        Attribute optionalAttr = (Attribute) schema.getEntities().get(0).getVariations().get(0).getFeatures().get(1);
        Attribute mandatoryAttr1 = (Attribute) schema.getEntities().get(0).getVariations().get(0).getFeatures().get(2);
        Attribute mandatoryAttr2 = (Attribute) schema.getEntities().get(0).getVariations().get(1).getFeatures().get(1);

        assertTrue(optionalAttr.isOptional());
        assertFalse(mandatoryAttr1.isOptional());
        assertFalse(mandatoryAttr2.isOptional());
    }
}

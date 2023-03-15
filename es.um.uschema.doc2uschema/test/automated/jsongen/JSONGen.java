/**
 *
 */
package automated.jsongen;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;

/**
 * @author dsevilla
 *
 */
public class JSONGen
{
  USchema schema;
    ObjectNode root;
    private ObjectMapper mapper;

    public JSONGen(USchema spec)
    {
        this.schema = spec;
    }

    public JSONGen doGen()
    {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        root = mapper.createObjectNode();

        ArrayNode rows = mapper.createArrayNode();
        root.set("rows", rows);

        genSchemas(rows);

        return this;
    }

    private void genSchemas(ArrayNode rows)
    {
        schema.getEntities().stream().forEach(e -> e.getVariations().forEach(v -> {
            ObjectNode row = mapper.createObjectNode();

            row.put("count", 0);
            row.put("firstTimestamp", 0);
            row.put("lastTimestamp", 0);
            // The schema is scaped as String
            row.put("schema", genSchema(e, v).toString());

            rows.add(row);
        }));
    }

    private ObjectNode genSchema(EntityType e, StructuralVariation v)
    {
        ObjectNode schema = mapper.createObjectNode();

        schema.put("_type", e.getName());

        v.getFeatures().stream().filter(Attribute.class::isInstance)
                .forEach(a -> schema.set(a.getName(), typeRepl(((Attribute) a).getType())));

        return schema;
    }

    private JsonNode typeRepl(DataType type)
    {
        if (type instanceof PrimitiveType)
        {
            String typename = ((PrimitiveType) type).getName();

            switch (typename.toLowerCase())
            {
            case "string":
                return mapper.valueToTree("s");
            case "int":
            case "number":
                return mapper.valueToTree(0);
            case "boolean":
                return mapper.valueToTree(true);
            case "null":
                return mapper.valueToTree(null);
            }
        }

        throw new IllegalArgumentException("Type " + type + " unrecognized.");
    }

    public ObjectNode getRoot()
    {
        return root;
    }

    public String getJSON()
    {
        String output = null;

        try
        {
            output = mapper.writeValueAsString(root);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return output;
    }
}

package es.um.unosql.doc2unosql.main.util;

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.um.unosql.doc2unosql.intermediate.raw.ArraySC;
import es.um.unosql.doc2unosql.intermediate.raw.BooleanSC;
import es.um.unosql.doc2unosql.intermediate.raw.NullSC;
import es.um.unosql.doc2unosql.intermediate.raw.NumberSC;
import es.um.unosql.doc2unosql.intermediate.raw.ObjectSC;
import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;
import es.um.unosql.doc2unosql.process.util.MakePair;

/**
 * @author dsevilla
 *
 */
public class RawSchemaGen
{
    public static SchemaComponent deSchema(String name, JsonNode n)
    {
        if (n.isObject())
            return deSchema(name, (ObjectNode) n);

        if (n.isArray())
            return deSchema(name, (ArrayNode) n);

        if (n.isBoolean())
            return deSchema(name, (BooleanNode) n);

        if (n.isInt())
            return deSchema(name, (IntNode) n);

        if (n.isFloatingPointNumber())
            return deSchema(name, (DoubleNode) n);

        if (n.isNull())
            return deSchema(name, (NullNode) n);

        return null;
    }

    private static SchemaComponent deSchema(String name, ObjectNode o)
    {
        ObjectSC schema = new ObjectSC();

        SortedSet<String> fields = new TreeSet<String>();
        o.fieldNames().forEachRemaining(fields::add);
        fields.forEach(f -> schema.add(MakePair.of(f, deSchema(f, o.get(f)))));

        return schema;
    }

    private static SchemaComponent deSchema(String name, ArrayNode a)
    {
        ArraySC schema = new ArraySC();
        a.forEach(e -> schema.add(deSchema(null, e)));

        return schema;
    }

    private static SchemaComponent deSchema(String name, BooleanNode b)
    {
        BooleanSC schema = new BooleanSC();
        return schema;
    }

    private static SchemaComponent deSchema(String name, IntNode i)
    {
        NumberSC schema = new NumberSC();
        return schema;
    }

    private static SchemaComponent deSchema(String name, DoubleNode i)
    {
        NumberSC schema = new NumberSC();
        return schema;
    }

    private static SchemaComponent deSchema(String name, NullNode n)
    {
        NullSC schema = new NullSC();
        return schema;
    }
}

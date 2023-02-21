package es.um.unosql.doc2unosql.main;

import java.io.File;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonArray;
import com.google.inject.Inject;

import es.um.unosql.doc2unosql.main.util.JSON2Schema;
import es.um.unosql.doc2unosql.util.abstractjson.IAJAdapter;
import es.um.unosql.doc2unosql.util.abstractjson.IAJArray;
import es.um.unosql.doc2unosql.util.abstractjson.except.JSONException;
import es.um.unosql.doc2unosql.util.abstractjson.impl.gson.GsonAdapter;
import es.um.unosql.doc2unosql.util.abstractjson.impl.gson.GsonArray;
import es.um.unosql.doc2unosql.util.abstractjson.impl.jackson.JacksonAdapter;
import es.um.unosql.doc2unosql.util.abstractjson.impl.jackson.JacksonArray;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaPackage;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class BuildUNoSQLSchema
{
    private uNoSQLSchema schema;

    @Inject
    private JSON2Schema json2schema;

    @Inject
    private UNoSQLSchemaWriter writer;

    @Inject
    public BuildUNoSQLSchema(JSON2Schema json2schema, UNoSQLSchemaWriter writer)
    {
        this.json2schema = json2schema;
        this.writer = writer;
    }

    public void buildFromGsonArray(String schemaName, JsonArray jArray)
    {
        buildFromArray(schemaName, new GsonArray(jArray), new GsonAdapter());
    }

    public void buildFromJacksonArray(String schemaName, ArrayNode jArray)
    {
        buildFromArray(schemaName, new JacksonArray(jArray), new JacksonAdapter());
    }

    public void buildFromArray(String schemaName, IAJArray objRows, IAJAdapter<?> adapter)
    {
        json2schema.setFactory(UNoSQLSchemaPackage.eINSTANCE.getUNoSQLSchemaFactory());
        json2schema.setAdapter(adapter);

        schema = json2schema.fromJSONArray(schemaName, objRows);
    }

    public uNoSQLSchema getUNoSQLSchema()
    {
        return schema;
    }

    public void buildFromFile(File inputFile) throws JSONException
    {
        json2schema.setFactory(UNoSQLSchemaPackage.eINSTANCE.getUNoSQLSchemaFactory());
        json2schema.setAdapter(new JacksonAdapter());

        schema = json2schema.fromJSONFile(inputFile);
    }

    public void writeToFile(File outputFile)
    {
        writer.write(schema, outputFile);
    }
}

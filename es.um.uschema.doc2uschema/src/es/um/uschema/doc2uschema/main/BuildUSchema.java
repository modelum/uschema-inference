package es.um.uschema.doc2uschema.main;

import java.io.File;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonArray;
import com.google.inject.Inject;

import es.um.uschema.USchema.USchemaPackage;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.main.util.JSON2Schema;
import es.um.uschema.doc2uschema.util.abstractjson.IAJAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.IAJArray;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;
import es.um.uschema.doc2uschema.util.abstractjson.impl.gson.GsonAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.impl.gson.GsonArray;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonArray;
import es.um.uschema.utils.EcoreModelIO;

public class BuildUSchema
{
    private USchema schema;

    @Inject
    private JSON2Schema json2schema;

    @Inject
    private EcoreModelIO writer;

    @Inject
    public BuildUSchema(JSON2Schema json2schema, EcoreModelIO writer)
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
        json2schema.setFactory(USchemaPackage.eINSTANCE.getUSchemaFactory());
        json2schema.setAdapter(adapter);

        schema = json2schema.fromJSONArray(schemaName, objRows);
    }

    public USchema getUSchema()
    {
        return schema;
    }

    public void buildFromFile(File inputFile) throws JSONException
    {
        json2schema.setFactory(USchemaPackage.eINSTANCE.getUSchemaFactory());
        json2schema.setAdapter(new JacksonAdapter());

        schema = json2schema.fromJSONFile(inputFile);
    }

    public void writeToFile(File outputFile)
    {
        writer.write(schema, outputFile.toPath());
    }
}

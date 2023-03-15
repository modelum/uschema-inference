package es.um.uschema.doc2uschema.main.util;

import java.io.File;

import com.google.inject.Inject;

import es.um.uschema.USchema.USchemaFactory;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.process.SchemaInference;
import es.um.uschema.doc2uschema.process.USchemaModelBuilder;
import es.um.uschema.doc2uschema.util.abstractjson.IAJAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.IAJArray;
import es.um.uschema.doc2uschema.util.abstractjson.IAJElement;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;

/**
 * @author dsevilla
 *
 * @param <JE>      JSON Element (Base object of the JSON hierarchy)
 * @param <Adapter> The specific adapter for this JSON Element used
 */
public class JSON2Schema
{
    @Inject
    private SchemaInference si;

    @Inject
    private USchemaModelBuilder builder;

    private USchemaFactory factory;

    private IAJAdapter<?> adapter;

    public JSON2Schema(USchemaFactory factory, IAJAdapter<?> adapter)
    {
        this.factory = factory;
        this.adapter = adapter;
    }

    public JSON2Schema()
    {
    }

    @Inject
    public JSON2Schema(SchemaInference si, USchemaModelBuilder builder)
    {
        this.si = si;
        this.builder = builder;
    }

    public void setFactory(USchemaFactory factory)
    {
        this.factory = factory;
    }

    public void setAdapter(IAJAdapter<?> adapter)
    {
        this.adapter = adapter;
    }

    public USchema fromJSONFile(File inputFile) throws JSONException
    {
        // Extract schema name from the file name
        String schemaName = inputFile.getName().split("\\.")[0];

        // Read the JSON file
        IAJElement root = adapter.readFromFile(inputFile);
        assert (root.isArray());

        // Try to get the "rows" element of the base JSON object. If not
        IAJArray rows;
//		IAJElement e;
//		if ((e = root.get("rows")) != null)
//			rows = e.asArray();
//		else
        rows = root.asArray();

        return fromJSONArray(schemaName, rows);
    }

    public USchema fromJSONArray(String schemaName, IAJArray rows)
    {
        return builder.build(factory, schemaName, si.infer(rows));
    }
}

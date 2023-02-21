package es.um.unosql.doc2unosql.main.util;

import java.io.File;

import com.google.inject.Inject;

import es.um.unosql.doc2unosql.process.SchemaInference;
import es.um.unosql.doc2unosql.process.UNoSQLModelBuilder;
import es.um.unosql.doc2unosql.util.abstractjson.IAJAdapter;
import es.um.unosql.doc2unosql.util.abstractjson.IAJArray;
import es.um.unosql.doc2unosql.util.abstractjson.IAJElement;
import es.um.unosql.doc2unosql.util.abstractjson.except.JSONException;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaFactory;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

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
    private UNoSQLModelBuilder builder;

    private UNoSQLSchemaFactory factory;

    private IAJAdapter<?> adapter;

    public JSON2Schema(UNoSQLSchemaFactory factory, IAJAdapter<?> adapter)
    {
        this.factory = factory;
        this.adapter = adapter;
    }

    public JSON2Schema()
    {
    }

    @Inject
    public JSON2Schema(SchemaInference si, UNoSQLModelBuilder builder)
    {
        this.si = si;
        this.builder = builder;
    }

    public void setFactory(UNoSQLSchemaFactory factory)
    {
        this.factory = factory;
    }

    public void setAdapter(IAJAdapter<?> adapter)
    {
        this.adapter = adapter;
    }

    public uNoSQLSchema fromJSONFile(File inputFile) throws JSONException
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

    public uNoSQLSchema fromJSONArray(String schemaName, IAJArray rows)
    {
        return builder.build(factory, schemaName, si.infer(rows));
    }
}

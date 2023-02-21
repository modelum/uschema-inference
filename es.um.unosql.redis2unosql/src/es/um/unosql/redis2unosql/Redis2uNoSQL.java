package es.um.unosql.redis2unosql;

import static es.um.unosql.redis2unosql.constants.Constants.SLASH;

import java.io.File;
import java.util.Map;

import es.um.unosql.redis2unosql.model.ModelDirector;
import es.um.unosql.redis2unosql.spark.ISparkProcess;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class Redis2uNoSQL
{
	private uNoSQLSchema uNoSQLSchema;
	private ISparkProcess sparkProcess;

	public Redis2uNoSQL(ISparkProcess process)
	{
		sparkProcess = process;
	}
	
	public void process(String databaseUrl, int databasePort, String databaseName) 
	{
		Map<String, Long> variationSchema = sparkProcess.process(databaseUrl, databasePort, databaseName);
		
		variationSchema.forEach((k, v) -> System.out.println(k + " - " + v));
		
		ModelDirector modelDirector = new ModelDirector();
		uNoSQLSchema = modelDirector.createModel(variationSchema);
	}


	public void toXMI(String outputUri)
	{
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();
		
		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputUri));
	}
}

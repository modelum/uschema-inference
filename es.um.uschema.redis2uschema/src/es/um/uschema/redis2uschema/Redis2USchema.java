package es.um.uschema.redis2uschema;

import static es.um.uschema.redis2uschema.constants.Constants.SLASH;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.redis2uschema.model.ModelDirector;
import es.um.uschema.redis2uschema.spark.ISparkProcess;

public class Redis2USchema
{
	private USchema uSchema;
	private ISparkProcess sparkProcess;

	public Redis2USchema(ISparkProcess process)
	{
		sparkProcess = process;
	}
	
	public void process(String databaseUrl, int databasePort, String databaseName) 
	{
		Map<String, Long> variationSchema = sparkProcess.process(databaseUrl, databasePort, databaseName);
		
		variationSchema.forEach((k, v) -> System.out.println(k + " - " + v));
		
		ModelDirector modelDirector = new ModelDirector();
		uSchema = modelDirector.createModel(variationSchema);
	}


	public void toXMI(String outputUri)
	{
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();

		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(uSchema, Path.of(outputUri));
	}
}

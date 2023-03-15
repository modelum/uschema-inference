package es.um.uschema.neo4j2uschema;

import static es.um.uschema.neo4j2uschema.constants.Constants.NEW_LINE;
import static es.um.uschema.neo4j2uschema.constants.Constants.SLASH;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.neo4j2uschema.model.Json2USchemaModel;
import es.um.uschema.neo4j2uschema.spark.SparkProcess;

public class Neo4j2USchema {

	private String databaseBolt;
	private String databaseUser;
	private String databasePassword;
	private String databaseName;
	private double samplingRatio;
	private Map<String, Long> schemaCounts;
	private USchema uSchema;

	public Neo4j2USchema(double samplingRatio, String databaseBolt, String databaseUser, String databasePassword, String databaseName) {
		this.databaseBolt = databaseBolt;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databaseName = databaseName;
		this.samplingRatio = samplingRatio;
	}

	public void process() throws IOException 
	{
		SparkProcess sparkProcess = new SparkProcess(samplingRatio, databaseBolt, databaseUser, databasePassword);
		Map<String, Long> schemaCounts = sparkProcess.process();
		
		Json2USchemaModel modelBuilder = new Json2USchemaModel(databaseName);
		modelBuilder.processArchetypes(schemaCounts);
		uSchema = modelBuilder.getUSchema();
	}
	
	public void saveJsonToFile(String jsonOutputUri) throws IOException
	{
		new File(jsonOutputUri.substring(0, jsonOutputUri.lastIndexOf(SLASH))).mkdirs();
		
		FileWriter fileWriter = new FileWriter(new File(jsonOutputUri)); 
		schemaCounts.forEach((key, value) -> {
			try {
				fileWriter.write(key + NEW_LINE);
				fileWriter.write(value + NEW_LINE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		fileWriter.close();
	}
	
	public void toXMI(String outputUri) {
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();
		
		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(uSchema, Path.of(outputUri));
	}
	
}

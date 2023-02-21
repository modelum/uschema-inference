package es.um.unosql.neo4j2unosql;

import static es.um.unosql.neo4j2unosql.constants.Constants.NEW_LINE;
import static es.um.unosql.neo4j2unosql.constants.Constants.SLASH;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import es.um.unosql.neo4j2unosql.model.Json2NoSQLSchemaModel;
import es.um.unosql.neo4j2unosql.spark.SparkProcess;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class Neo4j2uNoSQL {

	private String databaseBolt;
	private String databaseUser;
	private String databasePassword;
	private String databaseName;
	private double samplingRatio;
	private Map<String, Long> schemaCounts;
	private uNoSQLSchema uNoSQLSchema;

	public Neo4j2uNoSQL(double samplingRatio, String databaseBolt, String databaseUser, String databasePassword, String databaseName) {
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
		
		Json2NoSQLSchemaModel modelBuilder = new Json2NoSQLSchemaModel(databaseName);
		modelBuilder.processArchetypes(schemaCounts);
		uNoSQLSchema = modelBuilder.getuNoSQLSchema();
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
		
		UNoSQLSchemaWriter noSQLSchemaWriter = new UNoSQLSchemaWriter();
		noSQLSchemaWriter.write(uNoSQLSchema, new File(outputUri));
	}
	
}

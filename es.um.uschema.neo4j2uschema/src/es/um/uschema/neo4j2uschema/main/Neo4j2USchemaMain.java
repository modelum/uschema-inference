package es.um.uschema.neo4j2uschema.main;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.um.uschema.neo4j2uschema.Neo4j2USchema;

public class Neo4j2USchemaMain
{
	private static final String XMI_EXTENSION = ".xmi";
	private static final String JSON_EXTENSION = ".json";
	private static final String HADOOP_HOME_DIR = "hadoop.home.dir";
	private static final String HADOOP_DIR = "F:\\hadoop";
	
	private static final double SAMPLING_RATIO = 1.0;
	private static final String DATABASE_NAME = "UserProfile";
	private static final String DATABASE_BOLT = "bolt://localhost:7687";
	private static final String DATABASE_USER = "neo4j";
	private static final String DATABASE_PASSWORD = "test";
	
	private static final String OUTPUTS_FOLDER = "./outputs/";
	
	private static final boolean WRITE_INTERMEDIATE_JSON = false;
	
	// Small -> 12165ms
	// Small -> 11821ms
	// Small -> 12691ms
	// Medium -> 20121ms
	// Medium -> 20309ms
	// Medium -> 20177ms
	// Large -> 41814ms
	// Large -> 42116ms
	// Large -> 41448ms
	// Huge -> 109111ms
	// Huge -> 110608ms
	// Huge -> 109724ms
	public static void main(String[] args) throws IOException 
	{
		System.setProperty(HADOOP_HOME_DIR, HADOOP_DIR);
		
		Logger.getRootLogger().setLevel(Level.OFF);
		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {@Override public void write(int b) {}}) {});		
		
		long start = System.currentTimeMillis();
		
		Neo4j2USchema neo4j2uschema = new Neo4j2USchema(SAMPLING_RATIO, DATABASE_BOLT, DATABASE_USER, DATABASE_PASSWORD, DATABASE_NAME);
		neo4j2uschema.process();
		if (WRITE_INTERMEDIATE_JSON) 
		  neo4j2uschema.saveJsonToFile(OUTPUTS_FOLDER + DATABASE_NAME + JSON_EXTENSION);
		neo4j2uschema.toXMI(OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);

		long executionTime = System.currentTimeMillis() - start;
		showTime(executionTime);

		System.out.println("Model generated at: " + OUTPUTS_FOLDER);
	}

	private static void showTime(long result) 
	{
		System.out.println("Total Time: " + result + "ms (" + String.format("%d min, %02d sec" + ")", 
			    TimeUnit.MILLISECONDS.toMinutes(result), 
			    TimeUnit.MILLISECONDS.toSeconds(result) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
			)
		);
	}

}

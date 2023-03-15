package es.um.uschema.neo4j.queries.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.neo4j.queries.QueriesNeo4j;
import es.um.uschema.neo4j.queries.configuration.Neo4jSparkConfiguration;

public class ValidationLauncher
{
	private static final String APP_NAME = "neo4jqueries";
	private static final int THREADS = 1;
	private static final String URL = "bolt://127.0.0.1:7687";
	private static final String USER = "neo4j";
	private static final String PASSWORD = "test";
	
	private static final String MODEL_URI = "./inputs/UserProfile.xmi";

	private static final String VALIDATION_RESULTS_URI = "./outputs/ValidationResults.txt";

	public static void main(String[] args) throws IOException
	{
		long start = System.currentTimeMillis();

		performValidation(loadModel(), configuration());

		long executionTime = System.currentTimeMillis() - start;
		showTime(executionTime);
	}

	private static USchema loadModel()
	{
	  EcoreModelIO modelLoader = new EcoreModelIO();
		return modelLoader.load(USchema.class, Path.of(MODEL_URI));
	}
	
	private static Neo4jSparkConfiguration configuration()
	{
		Neo4jSparkConfiguration configuration = new Neo4jSparkConfiguration(APP_NAME)
				.local(THREADS)
				.url(URL)
				.user(USER)
				.password(PASSWORD);
		
		return configuration;
	}

	private static void performValidation(USchema uSchema, Neo4jSparkConfiguration configuration) 
			throws IOException
	{
		QueriesNeo4j neo4jQueries = new QueriesNeo4j(configuration);
		neo4jQueries.setValidation(true, false);
		neo4jQueries.countAll(uSchema);
		neo4jQueries.close();
		
		String validationResult = neo4jQueries.getStatisticsAsString();
		System.out.println(validationResult);
		
		writeValidationResults(validationResult);
	}

	private static void writeValidationResults(String validationResult) throws IOException
	{
		File file = new File(VALIDATION_RESULTS_URI.substring(0, VALIDATION_RESULTS_URI.lastIndexOf('/')));
		file.mkdirs();
		
		FileWriter fileWriter = new FileWriter(new File(VALIDATION_RESULTS_URI));
		fileWriter.write(validationResult);
		fileWriter.close();
		System.out.println("Output generated at: " + VALIDATION_RESULTS_URI);
	}

	private static void showTime(long result)
	{
		System.out.println("Total Time: " + 
				String.format("%d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(result),
				TimeUnit.MILLISECONDS.toSeconds(result) 
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))));
	}

}
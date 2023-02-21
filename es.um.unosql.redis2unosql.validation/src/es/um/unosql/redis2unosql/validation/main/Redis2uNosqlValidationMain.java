package es.um.unosql.redis2unosql.validation.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.ModelLoader;
import es.um.unosql.redis2unosql.validation.validation.queries.RedisValidator;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaPackage;

public class Redis2uNosqlValidationMain {

	private static final String DATABASE_HOST = "127.0.0.1";
	private static final int DATABASE_PORT = 6379;
	
	private static final String MODEL_URI = "./inputs/redis.xmi";
	private static final String VALIDATION_RESULTS_URI = "./outputs/ValidationResults.txt";

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		performValidation(loadModel());

		long executionTime = System.currentTimeMillis() - start;
		showTime(executionTime);
	}

	private static uNoSQLSchema loadModel() {
		ModelLoader modelLoader = new ModelLoader();
		modelLoader.registerPackages(UNoSQLSchemaPackage.eINSTANCE);
		File file = new File(MODEL_URI);

		return modelLoader.load(file, uNoSQLSchema.class);
	}

	private static void performValidation(uNoSQLSchema uNoSQLSchema) throws IOException {
		RedisValidator redisValidator = new RedisValidator(DATABASE_HOST, DATABASE_PORT);
		redisValidator.validate(uNoSQLSchema);
		redisValidator.close();
		
		String validationResult = redisValidator.getStatisticsAsString();
		
		System.out.println(validationResult);
		writeValidationResults(validationResult);
	}

	private static void writeValidationResults(String validationResult) throws IOException
	{
		File file = new File(VALIDATION_RESULTS_URI);
		file.getParentFile().mkdirs();

		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(validationResult);
		fileWriter.close();
		System.out.println("Output generated at: " + VALIDATION_RESULTS_URI);
	}

	private static void showTime(long result)
	{
		System.out.println("Total Time: " + String.format("%d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(result),
				TimeUnit.MILLISECONDS.toSeconds(result)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))));
	}

}
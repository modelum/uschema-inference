package es.um.uschema.redis2uschema.validation.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.redis2uschema.validation.validation.queries.RedisValidator;

public class Redis2USchemaValidationMain {

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

	private static USchema loadModel() {
	  EcoreModelIO modelLoader = new EcoreModelIO();
		return modelLoader.load(USchema.class, Path.of(MODEL_URI));
	}

	private static void performValidation(USchema uSchema) throws IOException {
		RedisValidator redisValidator = new RedisValidator(DATABASE_HOST, DATABASE_PORT);
		redisValidator.validate(uSchema);
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
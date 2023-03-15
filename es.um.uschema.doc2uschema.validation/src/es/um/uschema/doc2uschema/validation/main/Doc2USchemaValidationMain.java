package es.um.uschema.doc2uschema.validation.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.doc2uschema.validation.queries.MongoDBValidator;

public class Doc2USchemaValidationMain {

	private static final String DATABASE_PORT = "27017";
	private static final String DATABASE_HOST = "localhost";
	
	private static final String MODEL_URI = "./inputs/userProfile.xmi";
	private static final String VALIDATION_RESULTS_URI = "./outputs/ValidationResults.txt";

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		performValidation(loadModel());

		long executionTime = System.currentTimeMillis() - start;
		showTime(executionTime);
	}

	private static USchema loadModel() {
	  EcoreModelIO modelLoader = new EcoreModelIO();
		File file = new File(MODEL_URI);

		return modelLoader.load(USchema.class, file.toPath());
	}

	private static void performValidation(USchema uSchema) throws IOException {
		MongoDBValidator mongoDBValidator = new MongoDBValidator(DATABASE_HOST, DATABASE_PORT, uSchema.getName());
		mongoDBValidator.validate(uSchema);
		
		String validationResult = mongoDBValidator.getStatisticsAsString();
		
		System.out.println(validationResult);
		writeValidationResults(validationResult);
	}

	private static void writeValidationResults(String validationResult) throws IOException {
		File file = new File(VALIDATION_RESULTS_URI.substring(0, VALIDATION_RESULTS_URI.lastIndexOf('/')));
		file.mkdirs();

		FileWriter fileWriter = new FileWriter(new File(VALIDATION_RESULTS_URI));
		fileWriter.write(validationResult);
		fileWriter.close();
		System.out.println("Output generated at: " + VALIDATION_RESULTS_URI);
	}

	private static void showTime(long result) {
		System.out.println("Total Time: " + String.format("%d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(result),
				TimeUnit.MILLISECONDS.toSeconds(result)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))));
	}

}
package es.um.uschema.mongodb2uschema.spark.main;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.um.uschema.USchema.USchema;
import es.um.uschema.mongodb2uschema.spark.MongoDB2USchema;

public class MongoDB2USchemaMain {

	private static final String DATABASE_URL = "127.0.0.1";
	private static final int DATABASE_PORT = 27017;
	private static final String DATABASE_NAME = "user_profile_verylarge";

	private static final String HADOOP_HOME_DIR = "hadoop.home.dir";
	private static final String HADOOP_DIR = "F:\\hadoop";

	private static final String OUTPUTS_FOLDER = "./outputs/";
	private static final String XMI_EXTENSION = ".xmi";

	// Huge -> 28815ms
	// Large -> 13540ms
	// Medium -> 9903ms
	// Small -> 5187ms
	public static void main(String args[]) throws IOException {
		System.setProperty(HADOOP_HOME_DIR, HADOOP_DIR);

		Logger.getRootLogger().setLevel(Level.OFF);
		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
			@Override
			public void write(int b) {
			}
		}) {
		});

		long start = System.currentTimeMillis();

		MongoDB2USchema mongodb2uschema = new MongoDB2USchema();
		USchema schema = mongodb2uschema.mongodb2uschema(DATABASE_URL, DATABASE_PORT, DATABASE_NAME);
		mongodb2uschema.toXMI(schema, OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);

		long executionTime = System.currentTimeMillis() - start;
		showTime(executionTime);

		System.out.println("Model generated at: " + OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);
	}

	private static void showTime(long result) {
		System.out.println("Total Time: " + result + "ms ("
				+ String.format("%d min, %02d sec" + ")", TimeUnit.MILLISECONDS.toMinutes(result),
						TimeUnit.MILLISECONDS.toSeconds(result)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))));
	}

}
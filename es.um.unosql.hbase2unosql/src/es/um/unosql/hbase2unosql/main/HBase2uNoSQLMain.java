package es.um.unosql.hbase2unosql.main;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.um.unosql.hbase2unosql.HBase2uNoSQL;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

public class HBase2uNoSQLMain {

	private static final String DATABASE_URL = "127.0.0.1";
	private static final int DATABASE_PORT = 2181;
	private static final String DATABASE_NAME = "UserProfile";

	private static final String HADOOP_HOME_DIR = "hadoop.home.dir";
	private static final String HADOOP_DIR = "F:\\hadoop";

	private static final String OUTPUTS_FOLDER = "./outputs/";
	private static final String XMI_EXTENSION = ".xmi";

	// Small -> 5696ms
	// Small -> 5494ms
	// Small -> 5656ms
	// Medium -> 7031ms
	// Medium -> 6762ms
	// Medium -> 6728ms
	// Large -> 11686ms
	// Large -> 11485ms
	// Large -> 11408ms
	// Huge -> 49775ms
	// Huge -> 48601ms
	// Huge -> 48750ms
	public static void main(String args[]) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
		System.setProperty(HADOOP_HOME_DIR, HADOOP_DIR);

		Logger.getRootLogger().setLevel(Level.OFF);
		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
			@Override
			public void write(int b) {
			}
		}) {
		});

		long start = System.currentTimeMillis();

		HBase2uNoSQL hbase2unosql = new HBase2uNoSQL();
		uNoSQLSchema schema = hbase2unosql.hbase2unosql(DATABASE_URL, DATABASE_PORT, DATABASE_NAME);
		hbase2unosql.toXMI(schema, OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);

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
package es.um.unosql.redis2unosql.main;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.um.unosql.redis2unosql.Redis2uNoSQL;
import es.um.unosql.redis2unosql.spark.ISparkProcess;
import es.um.unosql.redis2unosql.spark.SparkProcess;

public class Redis2uNoSQLMain
{
    private static final String XMI_EXTENSION = ".xmi";

    private static final int DATABASE_PORT = 6379;
    private static final String DATABASE_URL = "localhost";
    private static final String DATABASE_NAME = "UserProfile";

    private static final String OUTPUTS_FOLDER = "./outputs/";

    // Small -> 11384ms
    // Small -> 11788ms
    // Small -> 11487ms
    // Medium -> 22677ms
    // Medium -> 22013ms
    // Medium -> 21322ms
    // Large -> 59556ms
    // Large -> 61505ms
    // Large -> 62089ms
    // Huge -> 247947ms
    // Huge -> 253483ms
    // Huge -> 252794ms
    public static void main(String[] args)
    {
        Logger.getRootLogger().setLevel(Level.DEBUG);
//		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
//			@Override
//			public void write(int b) {
//			}
//		}) {
//		});

        ISparkProcess sparkProcess = new SparkProcess();
        Redis2uNoSQL process = new Redis2uNoSQL(sparkProcess);

        long start = System.currentTimeMillis();

        process.process(DATABASE_URL, DATABASE_PORT, DATABASE_NAME);

        long executionTime = System.currentTimeMillis() - start;

        process.toXMI(OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);

        showTime(executionTime);

        System.out.println("Model generated at: " + OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION);
    }

    private static void showTime(long result)
    {
        System.out.println("Total Time: " + result + "ms ("
                + String.format("%d min, %02d sec" + ")", TimeUnit.MILLISECONDS.toMinutes(result),
                        TimeUnit.MILLISECONDS.toSeconds(result)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))));
    }

}

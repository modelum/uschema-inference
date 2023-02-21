package es.um.unosql.hbase2unosql;

import static es.um.unosql.hbase2unosql.constants.Constants.SLASH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import es.um.unosql.hbase2unosql.model.ModelDirector;
import es.um.unosql.hbase2unosql.spark.map.ArchetypeMapping;
import es.um.unosql.hbase2unosql.spark.map.JSONMapping;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class HBase2uNoSQL {
	private static final String HBASE_CONNECTION_HOST = "hbase.zookeeper.quorum";
	private static final String HBASE_CONNECTION_PORT = "hbase.zookeeper.property.clientPort";
	private static final String HBASE_CONNECTION_TIMEOUT = "timeout";
	private static final String APP_NAME = "HBase";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";

	private uNoSQLSchema uNoSQLSchema;

	public uNoSQLSchema hbase2unosql(String databaseUri, int databasePort, String databaseName)
			throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
		SparkConf sparkConf = new SparkConf().setAppName(APP_NAME).setMaster(MASTER)
				.set("spark.executor.heartbeatInterval", Integer.toString(2000000))
				.set("spark.network.timeout", Integer.toString(99999999)).set("spark.driver.memory", "4g")
				.set("spark.executor.memory", "4g");

		JavaSparkContext sc = new JavaSparkContext(sparkConf);

		Configuration config = HBaseConfiguration.create();
		config.set(HBASE_CONNECTION_HOST, databaseUri);
		config.setInt(HBASE_CONNECTION_PORT, databasePort);
		config.setInt(HBASE_CONNECTION_TIMEOUT, 60000);

		HBaseAdmin.available(config); // Check database availability
		JavaHBaseContext hc = new JavaHBaseContext(sc, config);
		Connection conn = ConnectionFactory.createConnection(config);
		Admin admin = conn.getAdmin();

		List<TableName> tables = Arrays.asList(admin.listTableNames());

		Map<String, Long> result = new HashMap<String, Long>();
		tables.forEach(table -> {
			Map<String, Long> variationSchema = process(hc, table);
			variationSchema.forEach((k, v) -> result.put(k, result.getOrDefault(k, 0L) + v));
		});

		sc.stop();
		
		ModelDirector modelDirector = new ModelDirector();
		uNoSQLSchema = modelDirector.createModel(result, databaseName);

		return uNoSQLSchema;
	}

	private Map<String, Long> process(JavaHBaseContext hc, TableName table) {
		String name = table.toString();
		Scan scan = new Scan();
		Map<String, Long> result = hc.hbaseRDD(table, scan).mapToPair(new ArchetypeMapping(name))
				.reduceByKey((count1, count2) -> count1 + count2).mapToPair(new JSONMapping()).collectAsMap();

		return result;
	}

	public void toXMI(uNoSQLSchema uNoSQLSchema, String outputRoute) 
	{
		new File(outputRoute.substring(0, outputRoute.lastIndexOf(SLASH))).mkdirs();
		
		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		Paths.get(outputRoute).getParent().toFile().mkdirs();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputRoute));
	}

	public void toXMI(String outputUri)
	{
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();

		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputUri));
	}
}
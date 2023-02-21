package es.um.unosql.mongodb2unosql.spark;

import static es.um.unosql.mongodb2unosql.spark.constants.Constants.SLASH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.spark.MongoSpark;

import es.um.unosql.mongodb2unosql.spark.map.ArchetypeMapping;
import es.um.unosql.mongodb2unosql.spark.map.JSONMapping;
import es.um.unosql.mongodb2unosql.spark.model.ModelDirector;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class MongoDB2uNoSQL {
	private static final String MONGODB_HOST = "spark.mongodb.input.uri";
	private static final String MONGODB_DATABASE = "spark.mongodb.input.database";
	private static final String MONGODB_COLLECTION = "spark.mongodb.input.collection";
	private static final String APP_NAME = "MongoDB";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";

	private uNoSQLSchema uNoSQLSchema;

	public uNoSQLSchema hbase2unosql(String databaseUri, int databasePort, String databaseName) throws IOException {
		MongoClient mongoClient = MongoClients.create();
		MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
		MongoIterable<String> collectionNames = mongoDatabase.listCollectionNames();

		Map<String, Long> result = new HashMap<String, Long>();
		collectionNames.forEach(collectionName -> {
			Map<String, Long> variationSchema = processCollection(databaseUri, databasePort, databaseName,
					collectionName);

			variationSchema.forEach((k, v) -> result.put(k, result.getOrDefault(k, 0L) + v));
		});

		ModelDirector modelDirector = new ModelDirector();
		uNoSQLSchema = modelDirector.createModel(result, databaseName);

		return uNoSQLSchema;
	}

	private Map<String, Long> processCollection(String databaseUri, int databasePort, String databaseName,
			String collectionName) {
		SparkSession spark = SparkSession.builder().appName(APP_NAME).master(MASTER)
				.config(MONGODB_HOST, "mongodb://" + databaseUri + ":" + databasePort + "/")
				.config(MONGODB_DATABASE, databaseName).config(MONGODB_COLLECTION, collectionName)
				.config("spark.executor.heartbeatInterval", Integer.toString(20000))
				.config("spark.network.timeout", Integer.toString(99999999)).config("spark.driver.memory", "4g")
				.config("spark.executor.memory", "4g").getOrCreate();

		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		Map<String, Long> variationSchema = MongoSpark.load(jsc).mapToPair(new ArchetypeMapping(collectionName))
				.reduceByKey((count1, count2) -> count1 + count2).mapToPair(new JSONMapping()).collectAsMap();
		jsc.stop();

		return variationSchema;
	}

	public void toXMI(uNoSQLSchema uNoSQLSchema, String outputRoute) {
		new File(outputRoute.substring(0, outputRoute.lastIndexOf(SLASH))).mkdirs();

		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		Paths.get(outputRoute).getParent().toFile().mkdirs();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputRoute));
	}

	public void toXMI(String outputUri) {
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();

		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputUri));
	}

}
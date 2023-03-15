package es.um.uschema.mongodb2uschema.spark;

import static es.um.uschema.mongodb2uschema.spark.constants.Constants.SLASH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.mongodb2uschema.spark.map.ArchetypeMapping;
import es.um.uschema.mongodb2uschema.spark.map.JSONMapping;
import es.um.uschema.mongodb2uschema.spark.model.ModelDirector;

public class MongoDB2USchema {
	private static final String MONGODB_HOST = "spark.mongodb.input.uri";
	private static final String MONGODB_DATABASE = "spark.mongodb.input.database";
	private static final String MONGODB_COLLECTION = "spark.mongodb.input.collection";
	private static final String APP_NAME = "MongoDB";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";

	private USchema uSchema;

	public USchema mongodb2uschema(String databaseUri, int databasePort, String databaseName) throws IOException {
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
		uSchema = modelDirector.createModel(result, databaseName);

		return uSchema;
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

	public void toXMI(USchema uSchema, String outputRoute) {
		new File(outputRoute.substring(0, outputRoute.lastIndexOf(SLASH))).mkdirs();

		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		Paths.get(outputRoute).getParent().toFile().mkdirs();
		uSchemaWriter.write(uSchema, Path.of(outputRoute));
	}

	public void toXMI(String outputUri) {
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();

		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(uSchema, Path.of(outputUri));
	}
}

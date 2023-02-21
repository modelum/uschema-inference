package es.um.unosql.mongodb2unosql;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;

import es.um.unosql.doc2unosql.process.SchemaInference;
import es.um.unosql.doc2unosql.process.UNoSQLModelBuilder;
import es.um.unosql.doc2unosql.util.abstractjson.IAJElement;
import es.um.unosql.doc2unosql.util.abstractjson.impl.jackson.JacksonAdapter;
import es.um.unosql.doc2unosql.util.config.SchemaInferenceConfig;
import es.um.unosql.mongodb2unosql.utils.Constants;
import es.um.unosql.mongodb2unosql.utils.Helpers;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaFactory;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaPackage;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class MongoDB2uNoSQL
{
	@Inject private SchemaInference si;
	@Inject private UNoSQLModelBuilder builder;
	@Inject private JacksonAdapter adapter;
	@Inject private SchemaInferenceConfig config;

	private UNoSQLSchemaFactory uNoSQLFactory;
	private ObjectMapper objectMapper;

	public MongoDB2uNoSQL()
	{
		this.uNoSQLFactory = UNoSQLSchemaPackage.eINSTANCE.getUNoSQLSchemaFactory();
		this.objectMapper = new ObjectMapper();
	}

	public void process(String databaseUri, String databaseName, String [] collections) throws JsonProcessingException, IOException
	{
		ArrayNode array = objectMapper.createArrayNode();
		for (String collectionName : collections)
			array.addAll(processEntity(databaseUri, databaseName, collectionName));

		IAJElement root = adapter.wrap(array);
		uNoSQLSchema schema = builder.build(uNoSQLFactory, databaseName, si.infer(root.asArray()));
		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(schema, new File("./outputs/model.xmi"));
	}

	private List<JsonNode> processEntity(String databaseUri, String databaseName, String collectionName)
	{
		String typeField = config.getTypeMarkerAttribute();
		SparkConf conf = new SparkConf()
				.setAppName(Constants.SPARK_APP_NAME)
				.setMaster(Constants.SPARK_MASTER)
				.set(Constants.SPARK_MONGODB_INPUT_URI, databaseUri)
				.set(Constants.SPARK_MONGODB_DATABASE_URI, databaseName)
				.set(Constants.SPARK_MONGODB_COLLECTION_URI, collectionName);


		JavaSparkContext jsc = new JavaSparkContext(conf);
		// Initialize MongoDB collection RDD
		JavaMongoRDD<Document> rddCollection = MongoSpark.load(jsc);
		List<JsonNode> docs = rddCollection
								// Combine multiple documents into a simplified one
								.mapToPair(doc -> Helpers.generateDocumentPair(doc))
								.reduceByKey((t1, t2) -> Helpers.reducePairs(t1, t2))
								// Collect results
								.collect()
								.stream()
								// Add entity type information and then generate JSON String
								.map(pair -> { pair._1.put(typeField, collectionName); return Helpers.documentPairToJSONNode(pair, objectMapper); })
								.collect(Collectors.toList());
		jsc.close();
		return docs;
	}
}
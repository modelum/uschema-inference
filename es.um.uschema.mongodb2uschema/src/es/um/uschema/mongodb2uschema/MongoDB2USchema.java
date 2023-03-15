package es.um.uschema.mongodb2uschema;

import java.io.IOException;
import java.nio.file.Path;
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

import es.um.uschema.doc2uschema.process.SchemaInference;
import es.um.uschema.doc2uschema.process.USchemaModelBuilder;
import es.um.uschema.doc2uschema.util.abstractjson.IAJElement;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonAdapter;
import es.um.uschema.doc2uschema.util.config.SchemaInferenceConfig;
import es.um.uschema.USchema.USchemaFactory;
import es.um.uschema.USchema.USchemaPackage;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.mongodb2uschema.utils.Constants;
import es.um.uschema.mongodb2uschema.utils.Helpers;

public class MongoDB2USchema
{
	@Inject private SchemaInference si;
	@Inject private USchemaModelBuilder builder;
	@Inject private JacksonAdapter adapter;
	@Inject private SchemaInferenceConfig config;

	private USchemaFactory uSchemaFactory;
	private ObjectMapper objectMapper;

	public MongoDB2USchema()
	{
		this.uSchemaFactory = USchemaPackage.eINSTANCE.getUSchemaFactory();
		this.objectMapper = new ObjectMapper();
	}

	public void process(String databaseUri, String databaseName, String [] collections) throws JsonProcessingException, IOException
	{
		ArrayNode array = objectMapper.createArrayNode();
		for (String collectionName : collections)
			array.addAll(processEntity(databaseUri, databaseName, collectionName));

		IAJElement root = adapter.wrap(array);
		USchema schema = builder.build(uSchemaFactory, databaseName, si.infer(root.asArray()));
		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(schema, Path.of("./outputs/model.xmi"));
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
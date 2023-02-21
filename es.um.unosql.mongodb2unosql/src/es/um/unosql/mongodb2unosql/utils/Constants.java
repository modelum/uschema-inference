package es.um.unosql.mongodb2unosql.utils;

import org.bson.types.ObjectId;

public class Constants 
{
	private static final String SPARK_MONGODB_CONFIGURATION_PREFIX = "spark.mongodb.input.";
	public static final String SPARK_MONGODB_INPUT_URI = SPARK_MONGODB_CONFIGURATION_PREFIX + "uri";
	public static final String SPARK_MONGODB_DATABASE_URI = SPARK_MONGODB_CONFIGURATION_PREFIX + "database";
	public static final String SPARK_MONGODB_COLLECTION_URI = SPARK_MONGODB_CONFIGURATION_PREFIX + "collection";
	public static final String SPARK_APP_NAME = "MongoDB";
	public static final String SPARK_MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";
	
	public static final String CONFIG_MONGODB_CONNECTION_KEY = "MONGO_URL";
	public static final String CONFIG_MONGODB_DATABASE_KEY = "MONGO_DATABASE";
	public static final String CONFIG_MONGODB_COLLECTIONS_KEY = "MONGO_COLLECTIONS";
	
	public static final String SIMPLE_DEFAULT_STRING = "";
	public static final Boolean SIMPLE_DEFAULT_BOOLEAN = false;
	public static final Integer SIMPLE_DEFAULT_INTEGER = 0;
	public static final Double SIMPLE_DEFAULT_DOUBLE = 0.0;
	public static final Long SIMPLE_DEFAULT_LONG = 0L;
	public static final ObjectId SIMPLE_DEFAULT_OBJECTID = new ObjectId("000000000000000000000000");
}

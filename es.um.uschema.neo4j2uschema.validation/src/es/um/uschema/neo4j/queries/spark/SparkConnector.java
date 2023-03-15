package es.um.uschema.neo4j.queries.spark;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;

import org.neo4j.spark.*;

import es.um.uschema.neo4j.queries.configuration.Neo4jSparkConfiguration;

public class SparkConnector
{
	
	private SparkContext sc;
	private SparkSession spark;
	private Neo4j neo4j;

	public SparkConnector(Neo4jSparkConfiguration configuration)
	{
		spark = SparkSession.builder()
				.appName("Neo4j2USchema")
				.config("spark.master", "local[4]")
				.config("spark.neo4j.bolt.user", "neo4j")
//				.config("spark.neo4j.bolt.password", "test")
				.config("spark.neo4j.bolt.url", "bolt://127.0.0.1:7687")
				.getOrCreate();
		spark.sparkContext().setLogLevel("WARN");
		
		sc = spark.sparkContext();
	}
	
	public Neo4j getNeo4jConnector()
	{
		if (neo4j == null)
		{
			createNewNeo4j();
		}
		
		return neo4j;
	}
	
	public void createNewNeo4j()
	{
		neo4j = new Neo4j(sc);
	}

	public void close()
	{
		spark.stop();
	}
}
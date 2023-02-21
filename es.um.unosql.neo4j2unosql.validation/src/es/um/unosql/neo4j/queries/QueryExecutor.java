package es.um.unosql.neo4j.queries;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.neo4j.spark.Neo4j;

import scala.collection.immutable.HashMap;

public class QueryExecutor
{
	private Neo4j neo4jConnector;

	public QueryExecutor(Neo4j neo4jConnector)
	{
		this.neo4jConnector = neo4jConnector;
	}

	public int count(String query)
	{
		return getAll(query).size();
	}

	public List<Row> getAll(String query)
	{
		JavaRDD<Row> javaRDD = neo4jConnector
				.cypher(query, new HashMap<String, Object>())
				.loadRowRdd()
				.toJavaRDD();
		List<Row> resultRows = javaRDD.collect();

		return resultRows;
	}

}
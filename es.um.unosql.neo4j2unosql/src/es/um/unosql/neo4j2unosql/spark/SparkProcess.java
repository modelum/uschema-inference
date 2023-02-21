package es.um.unosql.neo4j2unosql.spark;

import static es.um.unosql.neo4j2unosql.constants.Constants.EMPTY;
import static es.um.unosql.neo4j2unosql.constants.Constants.COLON;
import static es.um.unosql.neo4j2unosql.constants.Constants.GRAVE_ACCENT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Row;
import org.neo4j.spark.Neo4j;

import es.um.unosql.neo4j2unosql.spark.map.IdArchetypeMapping;
import es.um.unosql.neo4j2unosql.spark.map.SplitMapping;
import es.um.unosql.neo4j2unosql.spark.reduce.ReduceByIdArchetype;

public class SparkProcess
{
	private static final String APP_NAME = "Neo4j";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";
	private static final String SPARK_NEO4J_BOLT_URL = "spark.neo4j.bolt.url";
	@SuppressWarnings("unused")
	private static final String SPARK_NEO4J_BOLT_USER = "spark.neo4j.bolt.user";
	@SuppressWarnings("unused")
	private static final String SPARK_NEO4J_BOLT_PASSWORD = "spark.neo4j.bolt.password";
	
	private static final int LABELS_INDEX = 0;

	private double samplingRate;
	private String databaseUrl;
	@SuppressWarnings("unused")
	private String databaseUser;
	@SuppressWarnings("unused")
	private String databasePassword;

	public SparkProcess(double samplingRate, String databaseUrl, String databaseUser, String databasePassword)
	{
		if (samplingRate <= 0 || samplingRate > 1) throw new IllegalArgumentException("Sampling rate <= 0 or > 1, Value: " + samplingRate);
		this.samplingRate = samplingRate;
		this.databaseUrl = databaseUrl;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
	}
	
	public Map<String, Long> process()
	{
		SparkConf sparkConf = new SparkConf().setAppName(APP_NAME)
				.setMaster(MASTER)
				.set(SPARK_NEO4J_BOLT_URL, databaseUrl);
//				.set(SPARK_NEO4J_BOLT_USER, databaseUser)
//				.set(SPARK_NEO4J_BOLT_PASSWORD, databasePassword);
		SparkContext sparkContext = new SparkContext(sparkConf);
		Neo4j neo4j = new Neo4j(sparkContext);
		
		List<Row> labelsMinMaxCountRows = executeSimpleQuery(neo4j, generateLabelsMinMaxCountQuery());
		
		Map<String, Long> result = new HashMap<String, Long>();
		for (Row labelsMinMaxCountRow : labelsMinMaxCountRows)
		{
			List<String> labelsList = Stream.of((Object[]) labelsMinMaxCountRow.get(LABELS_INDEX))
					.filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toList());
			
			String labels = generateLabels(labelsList);
			processDatabase(neo4j, result, labelsList, labels);
		}
		
		sparkContext.stop();
		
		return result;
	}

	private void processDatabase(Neo4j neo4j, Map<String, Long> result, List<String> labelsList, String labels)
	{
		Map<String, Long> labelResult = executeQuery(neo4j, generateQuery(labelsList, labels));
		labelResult.forEach((k, v) -> result.put(k, result.get(k) != null ? result.get(k) + v : v));
	}

	private String generateLabels(List<String> labelsList)
	{
		String labels = EMPTY;
		if (!labelsList.isEmpty())
			labels = COLON + GRAVE_ACCENT + String.join(GRAVE_ACCENT + COLON + GRAVE_ACCENT, labelsList) + GRAVE_ACCENT;
		
		return labels;
	}
	
	private Map<String, Long> executeQuery(Neo4j neo4j, String query)
	{
		return neo4j.cypher(query, neo4j.cypher$default$2())
				.loadRowRdd().toJavaRDD()
				.mapToPair(new IdArchetypeMapping())
				.reduceByKey(new ReduceByIdArchetype())
				.flatMap(new SplitMapping())
				.countByValue();
	}
	
	private String generateLabelsMinMaxCountQuery()
	{
		return "MATCH (n) RETURN DISTINCT labels(n)";
	}
	
	private String generateQuery(List<String> labelsList, String labels)
	{
		return "MATCH (n" + labels + ") " +
				" WHERE size(labels(n)) = " + labelsList.size() + 
				" WITH n OPTIONAL MATCH (n)-[r]->(m)" + 
					(samplingRate != 1.0 ? " WHERE rand() < " + samplingRate : "") +
				" RETURN n, r, labels(m) ";// + PARTITIONS_BATCH_SIZE;
	}

	private List<Row> executeSimpleQuery(Neo4j neo4j, String query)
	{
		return neo4j.cypher(query, neo4j.cypher$default$2())
				.loadRowRdd().toJavaRDD()
				.collect();
	}
	
}

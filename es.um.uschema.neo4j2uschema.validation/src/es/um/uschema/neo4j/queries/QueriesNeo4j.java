package es.um.uschema.neo4j.queries;

import static es.um.uschema.neo4j.queries.constants.Constants.NODE_ID;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_LABELS;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_PROPERTIES;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_REL_IDS;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_REL_PROPERTIES;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_REL_TARGET_NODE_ID;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_REL_TARGET_NODE_TYPE;
import static es.um.uschema.neo4j.queries.constants.Constants.NODE_REL_TYPE;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_ID;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_ORIGIN_LABELS;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_ORIGIN_NODE_ID;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_PROPERTIES;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_TARGET_LABELS;
import static es.um.uschema.neo4j.queries.constants.Constants.REL_TARGET_NODE_ID;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.sql.Row;

import es.um.uschema.USchema.USchema;
import es.um.uschema.neo4j.queries.builder.Neo4jQueryBuilder;
import es.um.uschema.neo4j.queries.configuration.Neo4jSparkConfiguration;
import es.um.uschema.neo4j.queries.model.Neo4jNode;
import es.um.uschema.neo4j.queries.model.Neo4jReference;
import es.um.uschema.neo4j.queries.processors.PropertiesRowReductor;
import es.um.uschema.neo4j.queries.spark.SparkConnector;
import es.um.uschema.neo4j.queries.statistics.StatisticsMeter;
import es.um.uschema.neo4j.queries.utils.StringNameUtils;
import es.um.uschema.neo4j.queries.validation.exceptions.DuplicatedIdOnDifferentQueriesException;
import es.um.uschema.USchema.SchemaType;
import es.um.uschema.USchema.StructuralVariation;

public class QueriesNeo4j
{
	private static final int ID_INDEX = 0;
	private static final int SKIP_NUMBER = 100;
	private SparkConnector sparkConnector;
	private boolean validation;
	private StatisticsMeter statisticsMeter;
	private Set<Long> ids;
	private boolean findOne;
	
	public QueriesNeo4j(Neo4jSparkConfiguration configuration)
	{
		this.sparkConnector = new SparkConnector(configuration);
		this.validation = false;
	}

	public void countAll(USchema uSchema)
	{
	  uSchema.getEntities().forEach(et -> {
			if (validation) ids = new HashSet<Long>();
			et.getVariations().forEach(sv -> 
				count(sv)
			);
		});

	  uSchema.getRelationships().forEach(rt -> {
			if (validation) ids = new HashSet<Long>();
			rt.getVariations().forEach(sv -> 
				count(sv)
			);
		});
	}

	private List<Row> performQueryWithOneResult(StructuralVariation structuralVariation)
	{
		Neo4jQueryBuilder queryMaker = new Neo4jQueryBuilder(structuralVariation);
		String query = queryMaker.getQuery();
		
		QueryExecutor queryExecutor = new QueryExecutor(sparkConnector.getNeo4jConnector());
		PropertiesRowReductor propertiesRowReductor = new PropertiesRowReductor(structuralVariation);

		int skip = 0;
		List<Row> resultRows = null;
		do {
			String findOneQuery = query + "SKIP " + skip + " LIMIT " + SKIP_NUMBER;
			List<Row> queryResultRows = queryExecutor.getAll(findOneQuery);
			resultRows = propertiesRowReductor.reduceVariationRows(queryResultRows, findOne);
			
			skip += SKIP_NUMBER;
		} while (resultRows.size() == 0);
		
		if (validation)
		{
			checkIds(structuralVariation, query, resultRows);
			statisticsMeter.saveStructuralVariationStatistics(structuralVariation, resultRows, query);
		}
		
		return resultRows;
	}

	private List<Row> performQuery(StructuralVariation structuralVariation)
	{
		Neo4jQueryBuilder queryMaker = new Neo4jQueryBuilder(structuralVariation);
		String query = queryMaker.getQuery();
		
		QueryExecutor queryExecutor = new QueryExecutor(sparkConnector.getNeo4jConnector());
		List<Row> queryResultRows = queryExecutor.getAll(query);
		
		PropertiesRowReductor propertiesRowReductor = new PropertiesRowReductor(structuralVariation);
		List<Row> resultRows = propertiesRowReductor.reduceVariationRows(queryResultRows, false);
		
		if (validation)
		{
			checkIds(structuralVariation, query, resultRows);
			statisticsMeter.saveStructuralVariationStatistics(structuralVariation, resultRows, query);
		}
		
		return resultRows;
	}

	private void checkIds(StructuralVariation structuralVariation, String query, List<Row> resultRows)
	{
		resultRows.forEach(row -> {
			Long id = row.getLong(ID_INDEX);
			boolean added = ids.add(id);
			if (!added)
			{
				System.err.println("Duplicated ID as result of different queries: " + id);
				throw new DuplicatedIdOnDifferentQueriesException("Duplicated ID as result of different queries: " + id);
			}
		});
	}
	
	public int count(StructuralVariation structuralVariation)
	{
		if (validation && findOne)
			return performQueryWithOneResult(structuralVariation).size();
		
		return performQuery(structuralVariation).size();
	}
	
	public List<Neo4jNode> getNodes(StructuralVariation structuralVariation)
	{
		List<Row> processedResultRows = performQuery(structuralVariation);
		
		SchemaType classifier = (SchemaType) structuralVariation.eContainer();
		List<Neo4jNode> resultNodes = createNeo4jNodeList(classifier.getName(), processedResultRows);
		
		return resultNodes;
	}

	@SuppressWarnings("unchecked")
	private List<Neo4jNode> createNeo4jNodeList(String nodeName, List<Row> all)
	{
		List<Neo4jNode> resultNodes = new LinkedList<Neo4jNode>();
		
		for (Row row : all)
		{
			Long nodeId = (Long) row.get(NODE_ID);
			Map<String, Object> properties = (Map<String, Object>) row.get(NODE_PROPERTIES);
			List<Map<String, Object>> referencesProperties = (List<Map<String, Object>>) row.get(NODE_REL_PROPERTIES);
			List<Long> relsId = (List<Long>) row.get(NODE_REL_IDS);
			List<String> referenceType = (List<String>) row.get(NODE_REL_TYPE);
			List<String> targetLabels = (List<String>) row.get(NODE_REL_TARGET_NODE_TYPE);
			List<Long> targetNodeId = (List<Long>) row.get(NODE_REL_TARGET_NODE_ID);
			List<String> originLabels = (List<String>) row.get(NODE_LABELS);
			
			Neo4jNode neo4jNode = new Neo4jNode(nodeId.toString(), nodeName, properties, referencesProperties, referenceType, targetLabels, targetNodeId, originLabels, relsId);
			resultNodes.add(neo4jNode);
		}
		
		return resultNodes;
	}

	public List<Neo4jReference> getReferences(StructuralVariation structuralVariation)
	{
		List<Row> processedResultRows = performQuery(structuralVariation);
		
		SchemaType classifier = (SchemaType) structuralVariation.eContainer();
		List<Neo4jReference> resultNodes = createNeo4jReferenceList(classifier.getName(), processedResultRows);
		
		return resultNodes;
	}

	@SuppressWarnings("unchecked")
	private List<Neo4jReference> createNeo4jReferenceList(String type, List<Row> all)
	{
		List<Neo4jReference> resultNodes = new LinkedList<Neo4jReference>();
		
		for (Row row : all)
		{
			Long relId = (Long) row.get(REL_ID);
			Map<String, Object> properties = (Map<String, Object>) row.get(REL_PROPERTIES);
			List<Long> targetNodeIds = (List<Long>) row.get(REL_TARGET_NODE_ID);
			List<Long> originNodeIds = (List<Long>) row.get(REL_ORIGIN_NODE_ID);
			List<Object> originLabels = (List<Object>) row.get(REL_ORIGIN_LABELS);
			List<Object> targetLabels = (List<Object>) row.get(REL_TARGET_LABELS);
			for (int i = 0; i < targetNodeIds.size(); i++)
			{
				resultNodes.add(new Neo4jReference(relId.toString(), originNodeIds.get(i).toString(), properties, type, 
						Long.toString(targetNodeIds.get(i)), 
						StringNameUtils.targetLabels(originLabels.get(i)),
						StringNameUtils.targetLabels(targetLabels.get(i))));
			}
		}
		
		return resultNodes;
	}

	public void close()
	{
		sparkConnector.close();
	}
	
	public void setValidation(boolean validation)
	{
		this.validation = validation;
		if (validation)
		{
			this.statisticsMeter = new StatisticsMeter();
			this.ids = new HashSet<Long>();
		} 
	}
	
	public void setValidation(boolean validation, boolean findOne)
	{
		setValidation(validation);
		this.findOne = findOne;
		this.statisticsMeter.setFindOne(findOne);
	}
	
	public String getStatisticsAsString()
	{
		if (statisticsMeter == null)
		{
			return null;
		}
		
		return statisticsMeter.getStatisticsAsString();
	}
	
}
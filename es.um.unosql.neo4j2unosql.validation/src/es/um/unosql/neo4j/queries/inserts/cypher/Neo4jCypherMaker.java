package es.um.unosql.neo4j.queries.inserts.cypher;

import static es.um.unosql.neo4j.queries.constants.Constants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.unosql.neo4j.queries.model.Neo4jNode;
import es.um.unosql.neo4j.queries.model.Neo4jReference;
import es.um.unosql.neo4j.queries.model.Neo4jReferenceToNewObject;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

public class Neo4jCypherMaker
{

	public String createInsertCypherStatement(Neo4jReference reference)
	{
		String cypher = 
				"MATCH (a" + getLabels(reference.getOriginLabels()) + " {" + "_id:" + reference.getOriginId()  + "}) " + 
				"WITH a " + 
				"MATCH (b" + getLabels(reference.getTargetLabels()) + " {" + "_id:" + reference.getTargetNodeId()  + "}) " + 
				"CREATE (a)-[:" + reference.getType() + " { " + String.join(",", getPropertiesList(reference.getProperties())) + " }]->(b)";
		
		return cypher;
	}
	
	private String getLabels(String labels)
	{
		String labelsResult = "";
		if (labels != null) 
		{
			String[] labelsSplitted = labels.split("(?i)" + LABELS_JOINER);
			for (String label : labelsSplitted)
			{
				labelsResult += ":" + "`" + label + "`";
			}
		}
		
		return labelsResult;
	}

	private List<String> getPropertiesList(Map<String, Object> properties)
	{
		List<String> stringProperties = new LinkedList<String>();
		for (String propertyName : properties.keySet())
		{
			Object property = properties.get(propertyName);
			if (property instanceof String)
			{
				stringProperties.add(BACKTICK + propertyName + BACKTICK +": \"" + ((String)property).replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
			} 
			else
			{
				stringProperties.add(BACKTICK + propertyName + BACKTICK + ":" + property);
			}
		}
		
		return stringProperties;
	}

	public List<String> createIndexStatements(EntityType entityClass, String property, String createOrDrop)
	{
		String[] entitiesNames = entityClass.getName().split("(?i)" + LABELS_JOINER);
		List<String> statements = new LinkedList<String>();
		
		Arrays.stream(entitiesNames).forEach(n -> statements.add(createOrDrop + " INDEX ON :" + n + "(" + property + ")"));
		
		return statements;
	}

	public Set<String> createIndexStatements(uNoSQLSchema uNoSQLSchema, String property, String createOrDrop)
	{
		Set<String> statements = new HashSet<String>();
		for (EntityType entity : uNoSQLSchema.getEntities())
		{
			statements.addAll(createIndexStatements(entity, property, createOrDrop));
		}
		
		return statements;
	}

	public String createInsertCypherStatement(Neo4jReferenceToNewObject reference)
	{
		Neo4jNode targetNode = reference.getTargetNode();
		
		String cypher = 
				"MATCH (a" + getLabels(reference.getOriginLabels()).toLowerCase() + " {" + "_id: \"" + reference.getOriginId()  + "\" }) " + 
				"CREATE (a)-[:" + reference.getType() + " { " + String.join(",", getPropertiesList(reference.getProperties())) + " }]->" +
				"(b" + getLabels(targetNode.getName()) + " { " + String.join(",", getPropertiesList(targetNode.getProperties())) +  " } )";
		
		return cypher;
	}

}

package es.um.unosql.neo4j.queries.inserts;

import static es.um.unosql.neo4j.queries.constants.Constants.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import es.um.unosql.uNoSQLSchema.SchemaType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.neo4j.queries.configuration.Neo4jSparkConfiguration;
import es.um.unosql.neo4j.queries.inserts.cypher.Neo4jCypherMaker;
import es.um.unosql.neo4j.queries.inserts.cypher.Neo4jCypherStatementsStore;
import es.um.unosql.neo4j.queries.model.Neo4jNode;
import es.um.unosql.neo4j.queries.model.Neo4jReference;
import es.um.unosql.neo4j.queries.model.Neo4jReferenceToNewObject;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class Neo4jInsertsExecutor implements AutoCloseable
{
	private Driver driver;
	private Neo4jCypherStatementsStore neo4jCypherStatementsStore;

	public Neo4jInsertsExecutor(Neo4jSparkConfiguration configuration)
	{
		this.driver = GraphDatabase.driver(configuration.getUrl(), 
				AuthTokens.basic(configuration.getUser(), configuration.getPassword()));
		this.neo4jCypherStatementsStore = new Neo4jCypherStatementsStore();
	}
	
	public void insertNodes(List<Neo4jNode> nodes, boolean saveStatements)
	{
	    try (Session session = driver.session())
	    {
	        session.writeTransaction(new TransactionWork<Integer>()
	        {
	            @Override
	            public Integer execute(Transaction tx)
	            {
	            	List<Map<String, Object>> nodesProperties = new LinkedList<Map<String, Object>>();
	            	for (Neo4jNode neo4jNode : nodes)
					{
	            		Map<String, Object> properties = neo4jNode.getPropertiesCopy();
	            		properties.put("_id", neo4jNode.getId());
	            		nodesProperties.add(properties);
			        }
	            	tx.run("UNWIND $props AS properties CREATE (n" + getTypes(nodes) + ") SET n = properties", 
	            			Collections.singletonMap( "props", nodesProperties ));
	            	
	            	String json = createJsonListMapWithProperties(nodesProperties);
	            	neo4jCypherStatementsStore.add("UNWIND " + json + 
	            			" AS properties CREATE (n" + getTypes(nodes) + ") SET n = properties");
	            	
	            	return 1;
	            }
	        });
	    }
		
	}

	private String createJsonListMapWithProperties(List<Map<String, Object>> nodesProperties)
	{
		String json = "[";
    	List<String> jsonList = new LinkedList<String>();
    	for (Map<String, Object> map : nodesProperties)
		{
    		List<String> props = new LinkedList<String>();
			for (String key : map.keySet())
			{
				Object value = map.get(key);
				if (value instanceof String) 
				{
					String string = (String) value;
					props.add(key + ":" + "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
				} 
				else
				{
					props.add(key + ":" + value);
				}
			}
			jsonList.add("{" + String.join(",", props) + "}");
		}
    	json += String.join(",", jsonList) + "]";
		return json;
	}
	
	private String getTypes(List<Neo4jNode> nodes)
	{
		String types = "";
    	if (nodes.size() > 1) {
    		Neo4jNode neo4jNode = nodes.get(0);
    		String name = neo4jNode.getName();
    		String[] split = name.split("(?i)" + LABELS_JOINER);
    		for (String type : split)
			{
    			types += ":" + BACKTICK + type + BACKTICK;
			}
    	}
		return types;
	}

	public void insertRelationships(List<Neo4jReference> references, boolean saveStatements)
	{
		Neo4jCypherMaker cypherMaker = new Neo4jCypherMaker();

		try (Session session = driver.session())
		{
			for (Neo4jReference neo4jReference : references)
			{
				final String statement = cypherMaker.createInsertCypherStatement(neo4jReference);
				if (saveStatements)
				{
					neo4jCypherStatementsStore.add(statement);
				}
				
				session.writeTransaction(new TransactionWork<Integer>()
		        {
		            @Override
		            public Integer execute(Transaction tx)
		            {
						tx.run(statement);
						return 1;
		            }
		        });
				
			}
			
			session.close();
		}
	}
	
	public void createOrDropIndex(uNoSQLSchema uNoSQLSchema, String property, String createOrDrop)
	{
		Neo4jCypherMaker cypherMaker = new Neo4jCypherMaker();
		try (Session session = driver.session())
		{
			Set<String> statements = cypherMaker.createIndexStatements(uNoSQLSchema, property, createOrDrop);
			statements.stream().forEach(s -> session.run(s));
			session.close();
		}
	}
	
	public void insertRelationshipsToNewObject(List<Neo4jReferenceToNewObject> references, boolean saveStatements)
	{
		Neo4jCypherMaker cypherMaker = new Neo4jCypherMaker();

		try (Session session = driver.session())
		{
			for (Neo4jReferenceToNewObject neo4jReference : references)
			{
				final String statement = cypherMaker.createInsertCypherStatement(neo4jReference);
				if (saveStatements)
				{
					neo4jCypherStatementsStore.add(statement);
				}
				
				session.writeTransaction(new TransactionWork<Integer>()
		        {
		            @Override
		            public Integer execute(Transaction tx)
		            {
						tx.run(statement);
						return 1;
		            }
		        });
			}
			
			session.close();
		}
	}
	
	public Neo4jCypherStatementsStore getNeo4jCypherStatementsStore()
	{
		return neo4jCypherStatementsStore;
	}

	public void close() throws Exception
	{
		driver.close();
	}

	public void createAllReferences(Set<Reference> variationReferences)
	{
	    try (Session session = driver.session())
	    {
	        session.writeTransaction(new TransactionWork<Integer>()
	        {
	            @Override
	            public Integer execute(Transaction tx)
	            {
	            	for (Reference reference : variationReferences)
					{
	            		StructuralVariation structuralVariation = (StructuralVariation) reference.eContainer();
	            		SchemaType schemaType = (SchemaType) structuralVariation.eContainer();
	            		
	            		if (reference.getRefsTo() != null)
	            		{
		            		String statementTemplate = 
		            				"MATCH (origin:"  + "`" + schemaType.getName().toLowerCase() + "`" +  ") "
		            						+ "WHERE EXISTS(origin." + reference.getName() + ") WITH origin " +
		            				"MATCH (target:" + "`" + reference.getRefsTo().getName().toLowerCase() + "`" + ") "
		            						+ "WHERE (target._id " + (reference.getUpperBound() == 1 ? " = " : " IN ") +" origin." + reference.getName() + ") WITH origin, target " +
		            				"CREATE (origin)-[:" + "`" + reference.getName().toLowerCase() + "`" + "]->(target) ";
		            		
							tx.run(statementTemplate);
		            		neo4jCypherStatementsStore.add(statementTemplate);
	            		}
			        }
	            	
	            	return 1;
	            }
	        });
	    }
		
	}

}
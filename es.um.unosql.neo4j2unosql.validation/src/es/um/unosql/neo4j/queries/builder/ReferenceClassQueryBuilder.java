package es.um.unosql.neo4j.queries.builder;

import es.um.unosql.neo4j.queries.constants.Constants;
import es.um.unosql.uNoSQLSchema.RelationshipType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class ReferenceClassQueryBuilder
{
	private static final String ORIGIN_VAR = "n";
	private static final String RELATIONSHIP_VAR = "r";
	private static final String TARGET_VAR = "b";
	
	private StructuralVariation structuralVariation;
	private RelationshipType relationshipType;
	private PropertiesQueryBuilder propertiesQueryMaker;
	
	public ReferenceClassQueryBuilder(StructuralVariation structuralVariation, RelationshipType relationshipType)
	{
		this.structuralVariation = structuralVariation;
		this.relationshipType = relationshipType;
		this.propertiesQueryMaker = new PropertiesQueryBuilder();
	}

	public String getQuery()
	{
		String query =
			"MATCH (" + ORIGIN_VAR + ")-[" + RELATIONSHIP_VAR + ":" + structuralVariationLabel() + "]->(" + TARGET_VAR + ") " + 
			relationshipWhereClause() + 
			"WITH DISTINCT " + ORIGIN_VAR + "," + RELATIONSHIP_VAR + ", " + TARGET_VAR + " " +
			"RETURN id(" + RELATIONSHIP_VAR + "), properties(" + RELATIONSHIP_VAR + "), collect(id(" + TARGET_VAR + ")), " +
			"collect(id(" + ORIGIN_VAR + ")), collect(labels(" + ORIGIN_VAR + ")), collect(labels(" + TARGET_VAR + "))";
				
		return query;
	}

	private String structuralVariationLabel()
	{
		return relationshipType.getName();
	}
	
	private String relationshipWhereClause()
	{
		String whereClause = propertiesQueryMaker.whereClause(relationshipType, structuralVariation, RELATIONSHIP_VAR);
		
		if (! whereClause.isEmpty()) 
		{
			return "WHERE " + whereClause;
		}
		
		return Constants.EMPTY;
	}
	

}
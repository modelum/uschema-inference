package es.um.unosql.neo4j.queries.builder;

import static es.um.unosql.neo4j.queries.constants.Constants.EMPTY;
import static es.um.unosql.neo4j.queries.constants.Constants.LABELS_JOINER;
import static es.um.unosql.neo4j.queries.constants.Constants.SPACE;

import java.util.List;
import java.util.stream.Collectors;

import es.um.unosql.neo4j.queries.constants.Constants;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class EntityClassQueryBuilder
{
	private static final String AND = " AND ";
	private static final String WHERE = "WHERE ";
	
	private static final String NODE_VAR = "n";
	private static final String REL_VAR = "r";
	private static final String TARGET_VAR = "b";
	
	private StructuralVariation structuralVariation;
	private EntityType EntityType;
	private PropertiesQueryBuilder propertiesQueryMaker;
	private List<Reference> references;
	
	public EntityClassQueryBuilder(StructuralVariation structuralVariation, EntityType EntityType)
	{
		this.structuralVariation = structuralVariation;
		this.EntityType = EntityType;
		this.propertiesQueryMaker = new PropertiesQueryBuilder();
		
		this.references = structuralVariation.getFeatures().stream()
				.filter(Reference.class::isInstance)
				.map(Reference.class::cast)
				.collect(Collectors.toList());
	}

	public String getQuery()
	{
		String query =
			"MATCH (" + NODE_VAR + getLabels(EntityType.getName()) + ")" + SPACE +
			relationships() + SPACE + 
			nodeWhereClause() + SPACE + 
			"WITH " + NODE_VAR + SPACE + 
			"OPTIONAL MATCH (" + NODE_VAR + ")-[ " + REL_VAR + "]->(" + TARGET_VAR + ") " + 
			"WITH DISTINCT " + NODE_VAR + "," + REL_VAR + ", " + TARGET_VAR + " " +
			"RETURN id(" + NODE_VAR + "), properties(" + NODE_VAR + "), collect(properties(" + REL_VAR + ")), collect(id(" + REL_VAR + ")), "
					+ "collect(type(" + REL_VAR + ")), collect(labels(" + TARGET_VAR + ")), collect(id(" + TARGET_VAR + ")),"
					+ " labels(" + NODE_VAR + ")";
				
		return query;
	}

	private String getLabels(String name)
	{
		String[] labels = name.split(LABELS_JOINER);
		
		String result = "";
		for (String label : labels)
		{
			if (!label.isEmpty())
			{
				result += ":" + label;
			}
		}
		
		return result.length() == 1 ? EMPTY : result;
	}

	private String relationships()
	{
		String result = "";
		for (int i = 0; i < references.size(); i++)
		{
			Reference reference = references.get(i);
			String targetNodeLabels = getLabels( reference.getRefsTo().getName() );
			String relationshipLabel = getLabels( reference.getName() );
			result += ", (" + NODE_VAR + ")-[" + REL_VAR + i + relationshipLabel + "]->" + "(" + targetNodeLabels + ")";
		}
		
		return result;
	}

	private String nodeWhereClause()
	{
		String whereClause = propertiesQueryMaker.whereClause(EntityType, structuralVariation, NODE_VAR);
		whereClause = referencesWhereClause(whereClause);

		if (! whereClause.isEmpty()) 
		{
			return WHERE + sizeLabelStatements() + AND + whereClause;
		}
		
		return WHERE + sizeLabelStatements();
	}

	private String sizeLabelStatements()
	{
		if (EntityType.getName().isEmpty())
		{
			return "size(labels(" + NODE_VAR + ")) = 0 ";
		}
		
		return "size(labels(" + NODE_VAR + ")) = " + 
				EntityType.getName().split(Constants.LABELS_JOINER).length + " ";
	}

	private String referencesWhereClause(String whereClause)
	{
		for (int i = 0; i < references.size(); i++)
		{
			Reference reference = references.get(i);
			
			String referenceWhereClause = propertiesQueryMaker
					.whereClause(reference.getIsFeaturedBy(), REL_VAR + i);

			if (! referenceWhereClause.isEmpty()) 
			{
				whereClause += (whereClause.isEmpty() ? EMPTY : AND) + referenceWhereClause;
			}
		}
		
		return whereClause;
	}


}
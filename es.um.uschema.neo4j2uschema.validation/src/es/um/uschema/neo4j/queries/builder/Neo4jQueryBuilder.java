package es.um.uschema.neo4j.queries.builder;

import org.eclipse.emf.ecore.EObject;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.RelationshipType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.neo4j.queries.exceptions.ContainerUnknowException;

public class Neo4jQueryBuilder
{

	private StructuralVariation structuralVariation;

	public Neo4jQueryBuilder(StructuralVariation structuralVariation)
	{
		this.structuralVariation = structuralVariation;
	}

	public String getQuery()
	{
		EObject eContainer = structuralVariation.eContainer();
		if (eContainer instanceof EntityType)
		{
			EntityType EntityType = (EntityType) eContainer;
			EntityClassQueryBuilder entityClassQueryMaker = new EntityClassQueryBuilder(structuralVariation, EntityType);
			
			System.out.println(entityClassQueryMaker.getQuery());
			
			return entityClassQueryMaker.getQuery();
		} 
		else if (eContainer instanceof RelationshipType)
		{
			RelationshipType relationshipType = (RelationshipType) eContainer;
			ReferenceClassQueryBuilder referenceClassQueryMaker = new ReferenceClassQueryBuilder(structuralVariation, relationshipType);
			
			return referenceClassQueryMaker.getQuery();
		}
		
		throw new ContainerUnknowException("Container is null or Container's type is unknown");
	}
	
	public StructuralVariation getStructuralVariation()
	{
		return structuralVariation;
	}
	
}
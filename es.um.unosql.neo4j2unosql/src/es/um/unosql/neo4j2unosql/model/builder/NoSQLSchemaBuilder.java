package es.um.unosql.neo4j2unosql.model.builder;

import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaFactory;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.RelationshipType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.DataType;

import static es.um.unosql.neo4j2unosql.constants.Constants.*;

import es.um.unosql.neo4j2unosql.model.repository.NoSQLModelRepository;
import es.um.unosql.uNoSQLSchema.Attribute;

public class NoSQLSchemaBuilder
{
	private NoSQLModelRepository modelRepository;
	private UNoSQLSchemaFactory factory;
	
	public NoSQLSchemaBuilder(NoSQLModelRepository noSQLModelRepository)
	{
		this.modelRepository = noSQLModelRepository;
		this.factory = UNoSQLSchemaFactory.eINSTANCE;
	}

	public void createNoSQLSchema(String name)
	{
		uNoSQLSchema noSQLSchema = factory.createuNoSQLSchema();
		noSQLSchema.setName(name);
		
		modelRepository.saveuNoSQLSchema(noSQLSchema);
	}

	public EntityType getOrCreateEntityType(String name)
	{
		EntityType EntityType = modelRepository.getEntityClass(name);
		if (EntityType == null)
		{
			EntityType = createEntityType(name);
			modelRepository.saveEntityClass(EntityType);
		}
		
		return EntityType;
	}
	
	public EntityType createEntityType(String name)
	{
		EntityType entityType = factory.createEntityType();
		entityType.setName(name);
		entityType.setRoot(true);
		
		return entityType;
	}

	public Attribute createAttribute(String name, String type)
	{
		Attribute attribute = factory.createAttribute();
		attribute.setName(name);
		attribute.setType(createType(type));
		
		return attribute;
	}

	public DataType createType(String type)
	{
		PrimitiveType primitiveType = factory.createPrimitiveType();
		
		if (type.endsWith(ARRAY)) 
		{
			PList pList = factory.createPList();
			primitiveType.setName(type.substring(0, type.indexOf(ARRAY)));
			pList.setElementType(primitiveType);
			
			return pList;
		}
		else
		{
			primitiveType.setName(type);
			return primitiveType;
		}
	}


	public StructuralVariation createVariation(int variationId, long count)
	{
		StructuralVariation structuralVariation = factory.createStructuralVariation();
		structuralVariation.setCount(count);
		structuralVariation.setVariationId(variationId);
		
		return structuralVariation;
	}

	public Reference createReference(String type, String refsTo, StructuralVariation targetVariaton)
	{
		Reference reference = factory.createReference();
		reference.setName(type);
		reference.setLowerBound(0);
		reference.setUpperBound(1);
		reference.setRefsTo(getOrCreateEntityType(refsTo));
		reference.getIsFeaturedBy().add(targetVariaton);
		modelRepository.addReferenceToCount(type);
		
		return reference;
	}

	public RelationshipType getOrCreateRefType(String referenceName)
	{
		RelationshipType relationshipType = modelRepository.getReferenceClass(referenceName);
		if (relationshipType == null)
		{
			relationshipType = factory.createRelationshipType();
			relationshipType.setName(referenceName);
			modelRepository.saveReferenceClass(relationshipType);
		}
		
		return relationshipType;
	}

}

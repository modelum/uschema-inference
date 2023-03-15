package es.um.uschema.neo4j2uschema.model.builder;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.neo4j2uschema.model.repository.UModelRepository;
import es.um.uschema.USchema.USchemaFactory;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.RelationshipType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.DataType;

import static es.um.uschema.neo4j2uschema.constants.Constants.*;

import es.um.uschema.USchema.Attribute;

public class USchemaBuilder
{
	private UModelRepository modelRepository;
	private USchemaFactory factory;
	
	public USchemaBuilder(UModelRepository uModelRepository)
	{
		this.modelRepository = uModelRepository;
		this.factory = USchemaFactory.eINSTANCE;
	}

	public void createUSchema(String name)
	{
		USchema uSchema = factory.createUSchema();
		uSchema.setName(name);
		
		modelRepository.saveUSchema(uSchema);
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

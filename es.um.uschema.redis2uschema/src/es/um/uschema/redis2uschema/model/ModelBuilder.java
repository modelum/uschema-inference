package es.um.uschema.redis2uschema.model;

import static es.um.uschema.redis2uschema.constants.Constants.LEFT_SQUARE_BRACKET;
import static es.um.uschema.redis2uschema.constants.Constants.LIST;
import static es.um.uschema.redis2uschema.constants.Constants.RIGHT_SQUARE_BRACKET;
import static es.um.uschema.redis2uschema.constants.Constants.SET;

import java.util.HashMap;
import java.util.Map;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Key;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PSet;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.USchemaFactory;

public class ModelBuilder {

	private USchemaFactory uFactory;
	private USchema uSchema;

	private Map<String, EntityType> entities;

	public ModelBuilder() 
	{
		this.uFactory = new USchemaFactory();
		this.entities = new HashMap<String, EntityType>();
	}
	
	public EntityType getOrCreateEntityType(String entityName)
	{
		EntityType entityType = entities.get(entityName);
		
		if (entityType == null) {
			entityType = uFactory.createEntityType(entityName);
			uSchema.getEntities().add(entityType);
			entities.put(entityName, entityType);
		}
		
		return entityType;
	}
	
	public USchema createUSchema(String schemaName) 
	{
	  uSchema = uFactory.createUSchema(schemaName);
		return uSchema;
	}


	public StructuralVariation createStructuralVariation(EntityType entityType) 
	{
		StructuralVariation structuralVariation = uFactory.createStructuralVariation(entityType.getVariations().size() + 1);
		entityType.getVariations().add(structuralVariation);
		
		return structuralVariation;
	}
	
	public Attribute createAttribute(String name, StructuralVariation structuralVariation, DataType dataType) 
	{
		Attribute attribute = uFactory.createAttribute(name, dataType);
		attribute.setOptional(true);
		structuralVariation.getFeatures().add(attribute);
		structuralVariation.getStructuralFeatures().add(attribute);
		return attribute;
	}


	public Aggregate createAggregate(String aggrName, StructuralVariation structuralVariation)
	{
		Aggregate aggregate = uFactory.createAggregate(aggrName);
		aggregate.setOptional(true);
		aggregate.setLowerBound(1);
		aggregate.setUpperBound(1);
		structuralVariation.getFeatures().add(aggregate);
		structuralVariation.getStructuralFeatures().add(aggregate);
		
		return aggregate;
	}


	public Reference createReference(Attribute a, EntityType e) 
	{
		Reference reference = uFactory.createReference(a.getName());
		reference.getAttributes().add(a);
		reference.setLowerBound(1);
		if (a.getType() instanceof PrimitiveType) {
			reference.setUpperBound(1);
		} else {
			reference.setUpperBound(-1);
		}
		reference.setRefsTo(e);
		
		return reference;
	}
	
	public void createKey(String keyName, StructuralVariation structuralVariation, Attribute attribute)
	{
		Key key = uFactory.createKey(keyName);
		key.getAttributes().add(attribute);
		attribute.setKey(key);
		structuralVariation.getFeatures().add(key);
		structuralVariation.getLogicalFeatures().add(key);
	}

	public void createKey(String keyName, StructuralVariation structuralVariation)
	{
		Key key = uFactory.createKey(keyName);
		structuralVariation.getFeatures().add(key);
		structuralVariation.getLogicalFeatures().add(key);
	}
	
	public DataType createType(String type)
	{
		if (type.startsWith(LIST + LEFT_SQUARE_BRACKET) && type.endsWith(RIGHT_SQUARE_BRACKET))
		{
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uFactory.createPrimitiveType(collectionType);
			
			PList pList = uFactory.createPList(primitiveType);
			
			return pList;
		} else if (type.startsWith(SET + LEFT_SQUARE_BRACKET) && type.endsWith(RIGHT_SQUARE_BRACKET)) {
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uFactory.createPrimitiveType(collectionType);
			
			PSet pSet = uFactory.createPSet(primitiveType);
			
			return pSet;
		} else {
			PrimitiveType primitiveType = uFactory.createPrimitiveType(type);
			return primitiveType;
		}
	}

	
}

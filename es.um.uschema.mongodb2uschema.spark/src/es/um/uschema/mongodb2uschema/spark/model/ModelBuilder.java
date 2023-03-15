package es.um.uschema.mongodb2uschema.spark.model;

import static es.um.uschema.mongodb2uschema.spark.constants.Constants.*;

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

	private USchemaFactory uSchemaFactory;
	private USchema uSchema;

	private Map<String, EntityType> entities;

	public ModelBuilder() 
	{
		this.uSchemaFactory = new USchemaFactory();
		this.entities = new HashMap<String, EntityType>();
	}
	
	public EntityType getOrCreateEntityType(String entityName)
	{
		EntityType entityType = entities.get(entityName);
		
		if (entityType == null) {
			entityType = uSchemaFactory.createEntityType(entityName);
			uSchema.getEntities().add(entityType);
			entities.put(entityName, entityType);
		}
		
		return entityType;
	}
	
	public USchema createUSchema(String schemaName) 
	{
	  uSchema = uSchemaFactory.createUSchema(schemaName);
		return uSchema;
	}


	public StructuralVariation createStructuralVariation(EntityType entityType) 
	{
		StructuralVariation structuralVariation = uSchemaFactory.createStructuralVariation(entityType.getVariations().size() + 1);
		entityType.getVariations().add(structuralVariation);
		
		return structuralVariation;
	}
	
	public Attribute createAttribute(String name, StructuralVariation structuralVariation, DataType dataType) 
	{
		Attribute attribute = uSchemaFactory.createAttribute(name, dataType);
		attribute.setOptional(false);
		structuralVariation.getFeatures().add(attribute);
		structuralVariation.getStructuralFeatures().add(attribute);
		return attribute;
	}


	public Aggregate createAggregate(String aggrName, StructuralVariation structuralVariation)
	{
		Aggregate aggregate = uSchemaFactory.createAggregate(aggrName);
		aggregate.setOptional(false);
		aggregate.setLowerBound(1);
		aggregate.setUpperBound(1);
		structuralVariation.getFeatures().add(aggregate);
		structuralVariation.getStructuralFeatures().add(aggregate);
		
		return aggregate;
	}


	public Reference createReference(Attribute a, EntityType e) 
	{
		Reference reference = uSchemaFactory.createReference(null);
		reference.getAttributes().add(a);
		if (a.getType() instanceof PrimitiveType) {
			reference.setLowerBound(1);
			reference.setUpperBound(1);
		} else {
			reference.setLowerBound(0);
			reference.setUpperBound(-1);
		}
		reference.setRefsTo(e);
		
		return reference;
	}
	
	public Key createKey(String keyName, StructuralVariation structuralVariation, Attribute attribute)
	{
		Key key = createKey(keyName, structuralVariation);
		key.getAttributes().add(attribute);
		attribute.setKey(key);
		
		return key;
	}


	public Key createKey(StructuralVariation structuralVariation) {
		return createKey(null, structuralVariation);
	}


	public Key createKey(StructuralVariation structuralVariation, Attribute attribute) {
		Key key = createKey(null, structuralVariation);
		key.getAttributes().add(attribute);
		attribute.setKey(key);
		
		return key;
	}

	public Key createKey(String keyName, StructuralVariation structuralVariation)
	{
		Key key = uSchemaFactory.createKey(keyName);
		structuralVariation.getFeatures().add(key);
		structuralVariation.getLogicalFeatures().add(key);
		
		return key;
	}
	
	public DataType createType(String type)
	{
		if (type.equals(LEFT_SQUARE_BRACKET + RIGHT_SQUARE_BRACKET)) {
			PrimitiveType primitiveType = uSchemaFactory.createPrimitiveType("undefined");
			return uSchemaFactory.createPList(primitiveType);
		} else if ( (type.startsWith(LIST + LEFT_SQUARE_BRACKET) || type.startsWith(ARRAY + LEFT_SQUARE_BRACKET)) && type.endsWith(RIGHT_SQUARE_BRACKET))
		{
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uSchemaFactory.createPrimitiveType(collectionType);
			
			PList pList = uSchemaFactory.createPList(primitiveType);
			
			return pList;
		} else if (type.startsWith(SET + LEFT_SQUARE_BRACKET) && type.endsWith(RIGHT_SQUARE_BRACKET)) {
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uSchemaFactory.createPrimitiveType(collectionType);
			
			PSet pSet = uSchemaFactory.createPSet(primitiveType);
			
			return pSet;
		} else {
			PrimitiveType primitiveType = uSchemaFactory.createPrimitiveType(type);
			return primitiveType;
		}
	}
	
}

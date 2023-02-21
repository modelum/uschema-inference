package es.um.unosql.redis2unosql.model;

import static es.um.unosql.redis2unosql.constants.Constants.LIST;
import static es.um.unosql.redis2unosql.constants.Constants.SET;
import static es.um.unosql.redis2unosql.constants.Constants.LEFT_SQUARE_BRACKET;
import static es.um.unosql.redis2unosql.constants.Constants.RIGHT_SQUARE_BRACKET;

import java.util.HashMap;
import java.util.Map;

import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Key;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PSet;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLFactory;

public class ModelBuilder {

	private UNoSQLFactory uNoSQLFactory;
	private uNoSQLSchema uNoSQLSchema;

	private Map<String, EntityType> entities;

	public ModelBuilder() 
	{
		this.uNoSQLFactory = new UNoSQLFactory();
		this.entities = new HashMap<String, EntityType>();
	}
	
	public EntityType getOrCreateEntityType(String entityName)
	{
		EntityType entityType = entities.get(entityName);
		
		if (entityType == null) {
			entityType = uNoSQLFactory.createEntityType(entityName);
			uNoSQLSchema.getEntities().add(entityType);
			entities.put(entityName, entityType);
		}
		
		return entityType;
	}
	
	public uNoSQLSchema createUNoSQLSchema(String schemaName) 
	{
		uNoSQLSchema = uNoSQLFactory.createUNoSQLSchema(schemaName);
		return uNoSQLSchema;
	}


	public StructuralVariation createStructuralVariation(EntityType entityType) 
	{
		StructuralVariation structuralVariation = uNoSQLFactory.createStructuralVariation(entityType.getVariations().size() + 1);
		entityType.getVariations().add(structuralVariation);
		
		return structuralVariation;
	}
	
	public Attribute createAttribute(String name, StructuralVariation structuralVariation, DataType dataType) 
	{
		Attribute attribute = uNoSQLFactory.createAttribute(name, dataType);
		attribute.setOptional(true);
		structuralVariation.getFeatures().add(attribute);
		structuralVariation.getStructuralFeatures().add(attribute);
		return attribute;
	}


	public Aggregate createAggregate(String aggrName, StructuralVariation structuralVariation)
	{
		Aggregate aggregate = uNoSQLFactory.createAggregate(aggrName);
		aggregate.setOptional(true);
		aggregate.setLowerBound(1);
		aggregate.setUpperBound(1);
		structuralVariation.getFeatures().add(aggregate);
		structuralVariation.getStructuralFeatures().add(aggregate);
		
		return aggregate;
	}


	public Reference createReference(Attribute a, EntityType e) 
	{
		Reference reference = uNoSQLFactory.createReference(a.getName());
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
		Key key = uNoSQLFactory.createKey(keyName);
		key.getAttributes().add(attribute);
		attribute.setKey(key);
		structuralVariation.getFeatures().add(key);
		structuralVariation.getLogicalFeatures().add(key);
	}

	public void createKey(String keyName, StructuralVariation structuralVariation)
	{
		Key key = uNoSQLFactory.createKey(keyName);
		structuralVariation.getFeatures().add(key);
		structuralVariation.getLogicalFeatures().add(key);
	}
	
	public DataType createType(String type)
	{
		if (type.startsWith(LIST + LEFT_SQUARE_BRACKET) && type.endsWith(RIGHT_SQUARE_BRACKET))
		{
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uNoSQLFactory.createPrimitiveType(collectionType);
			
			PList pList = uNoSQLFactory.createPList(primitiveType);
			
			return pList;
		} else if (type.startsWith(SET + LEFT_SQUARE_BRACKET) && type.endsWith(RIGHT_SQUARE_BRACKET)) {
			String collectionType = type.substring(type.indexOf(LEFT_SQUARE_BRACKET) + 1 , type.lastIndexOf(RIGHT_SQUARE_BRACKET));
			PrimitiveType primitiveType = uNoSQLFactory.createPrimitiveType(collectionType);
			
			PSet pSet = uNoSQLFactory.createPSet(primitiveType);
			
			return pSet;
		} else {
			PrimitiveType primitiveType = uNoSQLFactory.createPrimitiveType(type);
			return primitiveType;
		}
	}

	
}

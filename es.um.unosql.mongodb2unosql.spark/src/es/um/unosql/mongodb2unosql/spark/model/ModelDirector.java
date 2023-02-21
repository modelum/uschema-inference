package es.um.unosql.mongodb2unosql.spark.model;

import static es.um.unosql.mongodb2unosql.spark.constants.Constants.ENTITY;
import static es.um.unosql.mongodb2unosql.spark.constants.Constants.PROPERTIES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.json.JSONArray;
import org.json.JSONObject;

import es.um.unosql.mongodb2unosql.spark.inflector.Inflector;
import es.um.unosql.mongodb2unosql.spark.utils.TypeUtils;
import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Feature;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralFeature;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

public class ModelDirector {

	private static final String _ID = "_id";

	private uNoSQLSchema uNoSQLSchema;
	private ModelBuilder modelBuilder;
	
	public ModelDirector() {
		this.modelBuilder = new ModelBuilder();
	}

	public uNoSQLSchema createModel(Map<String, Long> variationSchema, String databaseName)
	{
		uNoSQLSchema = modelBuilder.createUNoSQLSchema(databaseName);
		
		variationSchema.forEach((json, count) -> {
			JSONObject jsonObject = new JSONObject(json);
			String entityName = jsonObject.getString(ENTITY);
			
			EntityType entityType = modelBuilder.getOrCreateEntityType(entityName);
			entityType.setRoot(true);

			StructuralVariation structuralVariation = searchVariation(jsonObject, entityType);
			if (structuralVariation == null) {
				structuralVariation = modelBuilder.createStructuralVariation(entityType);
				structuralVariation.setCount(count);
			}

			JSONObject propertiesObject = jsonObject.getJSONObject(PROPERTIES);
			processJSOBObjectProperties(modelBuilder, entityName, structuralVariation, propertiesObject, count, entityType);
		});
		
		inferReferences();
		processOptionals();
		
		return uNoSQLSchema;
	}

	private void processJSOBObjectProperties(ModelBuilder modelBuilder, String entityName, StructuralVariation structuralVariation, JSONObject propertiesObject, long count, EntityType entityType) 
	{
		for (String propertyName : JSONObject.getNames(propertiesObject)) {
			Object property = propertiesObject.get(propertyName);
			
			if (property instanceof JSONObject)
				processObject(modelBuilder, structuralVariation, count, propertyName, property);
			else if (property instanceof JSONArray)
				processArray(modelBuilder, structuralVariation, count, propertyName, property);
			else 
				createAttribute(modelBuilder, structuralVariation, propertyName, property);
		}
	}

	private void processObject(ModelBuilder modelBuilder, StructuralVariation structuralVariation, long count, String propertyName, Object property)
	{
		JSONObject aggregateObject = (JSONObject) property;
		
		EntityType aggregateEntityType = modelBuilder.getOrCreateEntityType(Inflector.getInstance().singularize(propertyName));
		aggregateEntityType.setRoot(false);
		
		StructuralVariation aggregateStructuralVariation = searchVariation(aggregateObject, aggregateEntityType);
		if (aggregateStructuralVariation == null) {
			processNewAggregate(modelBuilder, structuralVariation, count, propertyName, aggregateObject.has(PROPERTIES) ? aggregateObject.getJSONObject(PROPERTIES) : aggregateObject, aggregateEntityType, false);
		} else {
			createNewAggregateFromExistingVariation(modelBuilder, structuralVariation, count, propertyName, aggregateObject.has(PROPERTIES) ? aggregateObject.getJSONObject(PROPERTIES) : aggregateObject, aggregateStructuralVariation, false);
		}
	}


	private void processArray(ModelBuilder modelBuilder, StructuralVariation structuralVariation, long count, String propertyName, Object property)
	{
		JSONArray jsonArray = (JSONArray) property;
		
		jsonArray.forEach(arrayElement -> {
			if (arrayElement instanceof JSONObject) {
				EntityType aggregateEntityType = modelBuilder.getOrCreateEntityType(Inflector.getInstance().singularize(propertyName));
				aggregateEntityType.setRoot(false);
				
				JSONObject aggregateObject = (JSONObject) arrayElement;
				
				StructuralVariation aggregateStructuralVariation = searchVariation(aggregateObject, aggregateEntityType);
				if (aggregateStructuralVariation == null) {
					processNewAggregate(modelBuilder, structuralVariation, count, propertyName, aggregateObject, aggregateEntityType, true);
				} else {
					createNewAggregateFromExistingVariation(modelBuilder, structuralVariation, count, propertyName, aggregateObject, aggregateStructuralVariation, true);
				}
			} else {
				Attribute attribute = createAttribute(modelBuilder, structuralVariation, propertyName, property);
				attribute.setOptional(true);
			}
		});
	}


	private Attribute createAttribute(ModelBuilder modelBuilder, StructuralVariation structuralVariation,
			String propertyName, Object property) {
		Attribute attribute = modelBuilder.createAttribute(propertyName, structuralVariation, modelBuilder.createType(TypeUtils.getTypeAsString(property)));
		if (propertyName.equals(_ID))
			modelBuilder.createKey(structuralVariation, attribute);
		
		return attribute;
	}


	private void createNewAggregateFromExistingVariation(ModelBuilder modelBuilder, StructuralVariation structuralVariation, long count, 
			String propertyName, JSONObject aggregateObject, StructuralVariation aggregateStructuralVariation, boolean isContainedInArray) {
		aggregateStructuralVariation.setCount(aggregateStructuralVariation.getCount() + count);
		
		Aggregate aggregate = modelBuilder.createAggregate(propertyName, structuralVariation);
		aggregate.getAggregates().add(aggregateStructuralVariation);
		structuralVariation.getFeatures().add(aggregate);
		structuralVariation.getStructuralFeatures().add(aggregate);
		
		if (isContainedInArray) {
			aggregate.setLowerBound(0);
			aggregate.setUpperBound(-1);
		}
	}

	private void processNewAggregate(ModelBuilder modelBuilder, StructuralVariation structuralVariation, long count,
			String propertyName, JSONObject aggregateObject, EntityType aggregateEntityType, boolean isContainedInArray) {
		StructuralVariation aggregateStructuralVariation;
		Optional<Aggregate> aggregateOptional = structuralVariation.getFeatures().stream().filter(f -> propertyName.equals(f.getName())).filter(Aggregate.class::isInstance).map(Aggregate.class::cast).findFirst();
		if (aggregateOptional.isPresent()) {
			Aggregate aggregate = aggregateOptional.get();
			aggregate.setLowerBound(0);
			aggregate.setUpperBound(-1);
			aggregateStructuralVariation = modelBuilder.createStructuralVariation(aggregateEntityType);
			aggregateStructuralVariation.setCount(count);
			aggregate.getAggregates().add(aggregateStructuralVariation);

			processJSOBObjectProperties(modelBuilder, propertyName, aggregateStructuralVariation, aggregateObject, count, aggregateEntityType);
		} else {
			aggregateStructuralVariation = modelBuilder.createStructuralVariation(aggregateEntityType);
			aggregateStructuralVariation.setCount(count);
			
			Aggregate aggregate = modelBuilder.createAggregate(propertyName, structuralVariation);
			aggregate.getAggregates().add(aggregateStructuralVariation);
			structuralVariation.getFeatures().add(aggregate);
			structuralVariation.getStructuralFeatures().add(aggregate);
			
			if (isContainedInArray) {
				aggregate.setLowerBound(0);
				aggregate.setUpperBound(-1);
			}
			
			processJSOBObjectProperties(modelBuilder, propertyName, aggregateStructuralVariation, aggregateObject, count, aggregateEntityType);
		}
	}

	private StructuralVariation searchVariation(JSONObject jsonObject, EntityType aggregateEntityType) {
		JSONObject propertiesObject = jsonObject;
		if (jsonObject.has(PROPERTIES))
			propertiesObject = jsonObject.getJSONObject(PROPERTIES);
		
		for (StructuralVariation sv : aggregateEntityType.getVariations()) {
			EList<StructuralFeature> structuralFeatures = sv.getStructuralFeatures();
			
			if (structuralFeatures.size() == JSONObject.getNames(propertiesObject).length) {
				int hits = 0;
				List<String> names = Arrays.asList(JSONObject.getNames(propertiesObject));
				for (Feature f : sv.getFeatures()) {
					if (names.contains(f.getName()))
						hits++;
				}
				
				if (hits == structuralFeatures.size() )
					return sv;
			}
		}
		
		return null;
	}

	private void inferReferences() 
	{
		Map<Attribute, EntityType> maps = createAttributeToEntityMap();
		createReferences(modelBuilder, maps);
	}

	private void createReferences(ModelBuilder modelBuilder, Map<Attribute, EntityType> maps)
	{
		maps.forEach((a, e) -> {
			EObject eContainer = a.eContainer();
			if (eContainer instanceof StructuralVariation) {
				StructuralVariation structuralVariation = (StructuralVariation) eContainer;

				Reference reference = modelBuilder.createReference(a, e);
				structuralVariation.getFeatures().add(reference);
				structuralVariation.getLogicalFeatures().add(reference);
			}
		});
	}

	private Map<Attribute, EntityType> createAttributeToEntityMap()
	{
		Map<Attribute, EntityType> maps = new HashMap<Attribute, EntityType>();
		
		uNoSQLSchema.getEntities().forEach(fE -> {
			fE.getVariations().forEach(fSV -> {
				
				uNoSQLSchema.getEntities().forEach(tE -> {
					String targetEntityName = tE.getName().toLowerCase();
					
					if (fE != tE) {
						fSV.getStructuralFeatures().stream().filter(Attribute.class::isInstance).map(Attribute.class::cast).forEach(a -> {
							String attributeName = a.getName().toLowerCase();
							
							if (attributeName.contains(targetEntityName) || targetEntityName.contains(attributeName) && 
									(Math.max(attributeName.length(), targetEntityName.length()) - Math.min(attributeName.length(), targetEntityName.length()) <= 2)) {
								maps.put(a, tE);
							}
						});
					}
				});
				
			});
		});
		
		return maps;
	}

	private void processOptionals()
	{
		AttributeOptionalsChecker attributeOptionalsChecker = new AttributeOptionalsChecker(uNoSQLSchema);
		attributeOptionalsChecker.processOptionals();
	}

}

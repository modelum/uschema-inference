package es.um.unosql.hbase2unosql.model;

import static es.um.unosql.hbase2unosql.constants.Constants.ENTITY;
import static es.um.unosql.hbase2unosql.constants.Constants.PROPERTIES;
import static es.um.unosql.hbase2unosql.constants.Constants.ROW_KEY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.json.JSONObject;

import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Feature;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralFeature;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

public class ModelDirector {

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
				modelBuilder.createKey(ROW_KEY, structuralVariation);
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
		for (String originalPropertyName : JSONObject.getNames(propertiesObject)) {
			Object property = propertiesObject.get(originalPropertyName);
			if (property instanceof JSONObject)
			{
				JSONObject aggregateObject = (JSONObject) property;
				
				String propertyName = originalPropertyName;
				EntityType aggregateEntityType = modelBuilder.getOrCreateEntityType(propertyName);
				aggregateEntityType.setRoot(false);
				
				StructuralVariation aggregateStructuralVariation = searchVariation(aggregateObject, aggregateEntityType);
				if (aggregateStructuralVariation == null) {
					Optional<Aggregate> aggregateOptional = structuralVariation.getFeatures().stream().filter(f -> f.getName().equals(propertyName)).filter(Aggregate.class::isInstance).map(Aggregate.class::cast).findFirst();
					if (aggregateOptional.isPresent()) {
						Aggregate aggregate = aggregateOptional.get();
						aggregateStructuralVariation = modelBuilder.createStructuralVariation(aggregateEntityType);
						aggregateStructuralVariation.setCount(count);
						aggregate.getAggregates().add(aggregateStructuralVariation);

						processJSOBObjectProperties(modelBuilder, propertyName, aggregateStructuralVariation, aggregateObject.getJSONObject(PROPERTIES), count, aggregateEntityType);
					} else {
						aggregateStructuralVariation = modelBuilder.createStructuralVariation(aggregateEntityType);
						aggregateStructuralVariation.setCount(count);
						
						Aggregate aggregate = modelBuilder.createAggregate(propertyName, structuralVariation);
						aggregate.getAggregates().add(aggregateStructuralVariation);
						structuralVariation.getFeatures().add(aggregate);
						structuralVariation.getStructuralFeatures().add(aggregate);
						
						processJSOBObjectProperties(modelBuilder, propertyName, aggregateStructuralVariation, aggregateObject.getJSONObject(PROPERTIES), count, aggregateEntityType);
					}
				} else {
					aggregateStructuralVariation.setCount(aggregateStructuralVariation.getCount() + count);
				}
			} else {
				modelBuilder.createAttribute(originalPropertyName, structuralVariation, modelBuilder.createType("Binary"));
			}
		}
	}

	private StructuralVariation searchVariation(JSONObject jsonObject, EntityType aggregateEntityType) {
		
		
		for (StructuralVariation sv : aggregateEntityType.getVariations()) {
			EList<StructuralFeature> structuralFeatures = sv.getStructuralFeatures();
			JSONObject propertiesObject = jsonObject.getJSONObject(PROPERTIES);
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

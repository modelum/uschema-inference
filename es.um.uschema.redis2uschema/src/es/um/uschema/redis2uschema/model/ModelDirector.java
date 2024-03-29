package es.um.uschema.redis2uschema.model;

import static es.um.uschema.redis2uschema.constants.Constants.ENTITY;
import static es.um.uschema.redis2uschema.constants.Constants.LEFT_SQUARE_BRACKET;
import static es.um.uschema.redis2uschema.constants.Constants.LIST;
import static es.um.uschema.redis2uschema.constants.Constants.PROPERTIES;
import static es.um.uschema.redis2uschema.constants.Constants.RIGHT_SQUARE_BRACKET;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.json.JSONObject;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;

public class ModelDirector {

	private USchema uSchema;
	private ModelBuilder modelBuilder;
	
	public ModelDirector() {
		this.modelBuilder = new ModelBuilder();
	}
	
	public USchema createModel( Map<String, Long> variationSchema)
	{
	  uSchema = modelBuilder.createUSchema("redis");
		
		variationSchema.forEach((json, count) -> {
			JSONObject jsonObject = new JSONObject(json);
			String entityName = jsonObject.getString(ENTITY);
			if (entityName.endsWith("*")) entityName = entityName.substring(0, entityName.indexOf("*"));
			
			EntityType entityType = modelBuilder.getOrCreateEntityType(entityName);
			entityType.setRoot(true);
			StructuralVariation structuralVariation = modelBuilder.createStructuralVariation(entityType);
			structuralVariation.setCount(count);
			
			JSONObject propertiesObject = jsonObject.getJSONObject(PROPERTIES);
			processJSOBObjectProperties(entityName, structuralVariation, propertiesObject, count, entityType);
		});
		
		inferReferences();
		processOptionals();
		
		return uSchema;
	}

	private void processJSOBObjectProperties(String entityName, StructuralVariation structuralVariation, JSONObject propertiesObject, long count, EntityType entityType) 
	{
		for (String originalPropertyName : JSONObject.getNames(propertiesObject)) {
			Object property = propertiesObject.get(originalPropertyName);
			if (property instanceof JSONObject)
			{
				JSONObject aggregateObject = (JSONObject) property;
				
				String propertyName = originalPropertyName;
				if (originalPropertyName.endsWith("*")) propertyName = propertyName.substring(0, propertyName.indexOf("*"));
				EntityType aggregateEntityType = modelBuilder.getOrCreateEntityType(propertyName);
				aggregateEntityType.setRoot(false);
				
				StructuralVariation aggregateStructuralVariation = searchVariation(aggregateObject, aggregateEntityType);
				if (aggregateStructuralVariation == null) {
					aggregateStructuralVariation = modelBuilder.createStructuralVariation(aggregateEntityType);
					aggregateStructuralVariation.setCount(count);
					
					Aggregate aggregate = modelBuilder.createAggregate(propertyName, structuralVariation);
					aggregate.getAggregates().add(aggregateStructuralVariation);
					structuralVariation.getFeatures().add(aggregate);
					structuralVariation.getStructuralFeatures().add(aggregate);
					
					if (originalPropertyName.endsWith("*")) {
						modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + propertyName + ".<index>.", structuralVariation);
					} else {
						modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + propertyName, structuralVariation);
					}
					
					processJSOBObjectProperties(propertyName, aggregateStructuralVariation, aggregateObject, count, aggregateEntityType);
				} else {
					aggregateStructuralVariation.setCount(aggregateStructuralVariation.getCount() + count);
					
					Aggregate aggregate = modelBuilder.createAggregate(propertyName, structuralVariation);
					aggregate.getAggregates().add(aggregateStructuralVariation);
					structuralVariation.getFeatures().add(aggregate);
					structuralVariation.getStructuralFeatures().add(aggregate);
					
					if (originalPropertyName.endsWith("*")) {
						modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + propertyName + ".<index>.", structuralVariation);
					} else {
						modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + propertyName, structuralVariation);
					}
				}
			} else if (property instanceof String) {
				String propertyValue = (String) property;
				if (originalPropertyName.endsWith("*")) {
					if (originalPropertyName.endsWith("*")) originalPropertyName = originalPropertyName.substring(0, originalPropertyName.indexOf("*"));
					Attribute attribute = modelBuilder.createAttribute(originalPropertyName, structuralVariation, modelBuilder.createType(LIST + LEFT_SQUARE_BRACKET + propertyValue + RIGHT_SQUARE_BRACKET));
					modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + originalPropertyName + ".<index>.", structuralVariation, attribute);
				} else {
					Attribute attribute = modelBuilder.createAttribute(originalPropertyName, structuralVariation, modelBuilder.createType(propertyValue));
					modelBuilder.createKey(entityName + (entityType.isRoot() ? ":<id>:" : ".") + originalPropertyName, structuralVariation, attribute);
				}
			}
		}
	}

	private StructuralVariation searchVariation(JSONObject propertiesObject, EntityType aggregateEntityType) {
		for (StructuralVariation sv : aggregateEntityType.getVariations()) {
			
			if (sv.getStructuralFeatures().size() == JSONObject.getNames(propertiesObject).length) {
				int hits = 0;
				List<String> names = Arrays.asList(JSONObject.getNames(propertiesObject));
				for (Feature f : sv.getFeatures()) {
					if (names.contains(f.getName())) {
						hits++;
					}
				}
				
				if (hits == sv.getStructuralFeatures().size() ) {
					return sv;
				}
			}
		}
		
		return null;
	}

	public void inferReferences() 
	{
		Map<Attribute, EntityType> maps = createAttributeToEntityMap();
		createReferences(maps);
	}

	private void createReferences(Map<Attribute, EntityType> maps)
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
		
		uSchema.getEntities().forEach(fE -> {
			fE.getVariations().forEach(fSV -> {
				
			  uSchema.getEntities().forEach(tE -> {
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


	public void processOptionals()
	{
		AttributeOptionalsChecker attributeOptionalsChecker = new AttributeOptionalsChecker(uSchema);
		attributeOptionalsChecker.processOptionals();
	}
	
	public USchema getUSchema() {
		return uSchema;
	}
	
}

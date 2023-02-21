package es.um.unosql.neo4j2unosql.model.builder;

import static es.um.unosql.neo4j2unosql.constants.Constants.ENTITY;
import static es.um.unosql.neo4j2unosql.constants.Constants.LABELS;
import static es.um.unosql.neo4j2unosql.constants.Constants.LABELS_JOINER;
import static es.um.unosql.neo4j2unosql.constants.Constants.NODE;
import static es.um.unosql.neo4j2unosql.constants.Constants.PROPERTIES;
import static es.um.unosql.neo4j2unosql.constants.Constants.REFERENCES;
import static es.um.unosql.neo4j2unosql.constants.Constants.REFS_TO;
import static es.um.unosql.neo4j2unosql.constants.Constants.RELATIONSHIP;
import static es.um.unosql.neo4j2unosql.constants.Constants.TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import es.um.unosql.neo4j2unosql.constants.Constants;
import es.um.unosql.neo4j2unosql.model.repository.NoSQLModelRepository;
import es.um.unosql.neo4j2unosql.utils.TypeUtils;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.RelationshipType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class StructuralVariationBuilder
{
	private NoSQLModelRepository noSQLModelRepository;
	private NoSQLSchemaBuilder noSQLBuilder;

	public StructuralVariationBuilder(NoSQLSchemaBuilder noSQLBuilder, NoSQLModelRepository noSQLModelRepository)
	{
		this.noSQLBuilder = noSQLBuilder;
		this.noSQLModelRepository = noSQLModelRepository;
	}

	public void createVariation(JSONObject archetypeJson, Long count)
	{
		String entity = archetypeJson.optString(ENTITY);
		
		if (NODE.equals(entity)) 
		{
			List<String> labels = new ArrayList<String>();
			archetypeJson.getJSONArray(LABELS).forEach(l -> labels.add(l.toString()));
			getOrCreateEntityTypeAndVariation(archetypeJson, String.join(LABELS_JOINER, labels), count);
		} else if (RELATIONSHIP.equals(entity)) 
		{
			String type = archetypeJson.getString(TYPE);
			getOrCreateRefTypeAndVariation(archetypeJson, type, count);
		}
	}

	private void getOrCreateEntityTypeAndVariation(JSONObject archetypeJson, String labels, Long count)
	{
		EntityType entityType = noSQLBuilder.getOrCreateEntityType(labels);
		createParentsEntities(entityType, labels);
		
		int newVariationId = entityType.getVariations().size() + 1;
		StructuralVariation variation = noSQLBuilder.createVariation(newVariationId, count);
		entityType.getVariations().add(variation);
		
		processProperties(archetypeJson.getJSONObject(PROPERTIES), variation);
		processReferences(archetypeJson.optJSONArray(REFERENCES), variation);
	}

	private void createParentsEntities(EntityType EntityType, String labels)
	{
		String[] labelsSplitted = labels.split(Constants.LABELS_JOINER);
		if (labelsSplitted.length > 1) {
			for (String label : labelsSplitted)
			{
				EntityType parentEntityClass = noSQLBuilder.getOrCreateEntityType(label);
				EntityType.getParents().add(parentEntityClass);
			}
		}
	}

	private void processProperties(JSONObject properties, StructuralVariation structuralVariation)
	{
		properties.keys().forEachRemaining(name -> {
				Attribute attribute = noSQLBuilder.createAttribute(name, TypeUtils.getTypeName(properties.get(name)));
				structuralVariation.getFeatures().add(attribute);
				structuralVariation.getStructuralFeatures().add(attribute);
			}
		);
	}

	private void processReferences(JSONArray relationships, StructuralVariation variation)
	{
		Map<String,Reference> references = new HashMap<String, Reference>();
		relationships.forEach(r -> {
			if (r instanceof JSONObject) 
			{
				JSONObject rel = (JSONObject) r;
				String type = rel.getString(TYPE);
				List<String> labels = new ArrayList<String>();
				rel.getJSONArray(REFS_TO).forEach(l -> labels.add(l.toString()));
				String refsTo = String.join(LABELS_JOINER, labels);
				
				StructuralVariation featuresVariation = getOrCreateRefTypeAndVariation(rel, type, 0L);	// Updated at postprocess.
				getOrCreateReference(variation, references, type, featuresVariation, refsTo);
			}
		});
	}

	private void getOrCreateReference(StructuralVariation variation, Map<String, Reference> references, String type,
			StructuralVariation featuresVariation, String refsTo)
	{
		Reference reference = references.get(type + refsTo);
		if (reference == null)
		{
			reference = noSQLBuilder.createReference(type, refsTo, featuresVariation);
			reference.setLowerBound(1);
			references.put(type + refsTo, reference);
			variation.getFeatures().add(reference);
			variation.getLogicalFeatures().add(reference);
		} else
		{
			reference.getIsFeaturedBy().add(featuresVariation);
			reference.setUpperBound(-1);
			noSQLModelRepository.addReferenceToCount(type);
		}
	}
	
	private StructuralVariation getOrCreateRefTypeAndVariation(JSONObject relationship, String referenceName, Long count)
	{
		RelationshipType relationshipType = noSQLBuilder.getOrCreateRefType(referenceName);
		StructuralVariation variation = getOrCreateVariation(relationship, relationshipType);
		
		if (variation.getCount() > 0L)
			variation.setCount(variation.getCount() + count);
		else if (count != 0L)
			variation.setCount(count);
		 
		return variation;
	}

	private StructuralVariation getOrCreateVariation(JSONObject relationship, RelationshipType relationshipType)
	{
		JSONObject properties = relationship.getJSONObject(PROPERTIES);
		StructuralVariation variation = noSQLModelRepository.getRefVariation(relationshipType, properties.toString());
		
		if (variation == null)
		{
			variation = noSQLBuilder.createVariation(noSQLModelRepository.getRefVariationId(relationshipType) , 0L);
			relationshipType.getVariations().add(variation);
			
			processProperties(properties, variation);
			noSQLModelRepository.saveRefVariation(relationshipType, properties.toString(), variation);
		}

		return variation;
	}

}
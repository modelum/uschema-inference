package es.um.unosql.neo4j2unosql.model.repository;

import java.util.HashMap;
import java.util.Map;

import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.uNoSQLSchema.RelationshipType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class NoSQLModelRepository 
{
	private uNoSQLSchema noSQLSchema;
	private Map<String, EntityType> entites;
	private Map<String, RelationshipType> referenceClasses;
	private Map<RelationshipType, Map<String, StructuralVariation>> referencesStructuralVariations;
	private Map<String, Integer> referencesCount;

	public NoSQLModelRepository() 
	{
		this.entites = new HashMap<String, EntityType>();
		this.referenceClasses = new HashMap<String, RelationshipType>();
		this.referencesStructuralVariations = new HashMap<RelationshipType, Map<String, StructuralVariation>>();
		this.referencesCount = new HashMap<String, Integer>();
	}
	
	public void saveuNoSQLSchema(uNoSQLSchema noSQLSchema)
	{
		this.noSQLSchema = noSQLSchema;
	}
	
	public uNoSQLSchema getNoSQLSchema()
	{
		return noSQLSchema;
	}

	public EntityType getEntityClass(String name)
	{
		return entites.get(name);
	}
	
	public void saveEntityClass(EntityType entity) 
	{
		if (!entites.containsKey(entity.getName())) {
			entites.put(entity.getName(), entity);
			noSQLSchema.getEntities().add(entity);
		}
	}

	public RelationshipType getReferenceClass(String referenceName)
	{
		return referenceClasses.get(referenceName);
	}

	public void saveReferenceClass(RelationshipType relationshipType)
	{
		noSQLSchema.getRelationships().add(relationshipType);
		
		referenceClasses.put(relationshipType.getName(), relationshipType);
		Map<String, StructuralVariation> structuralVariationsMap = new HashMap<String, StructuralVariation>();
		referencesStructuralVariations.put(relationshipType, structuralVariationsMap);
	}

	public StructuralVariation getRefVariation(RelationshipType RelationshipType, String relProperties)
	{
		Map<String, StructuralVariation> structuralVariationMap = referencesStructuralVariations.get(RelationshipType);
		return structuralVariationMap.get(relProperties);
	}

	public void saveRefVariation(RelationshipType RelationshipType, String relProperties, StructuralVariation referenceStructuralVariation)
	{
		Map<String, StructuralVariation> structuralVariationMap = referencesStructuralVariations.get(RelationshipType);
		structuralVariationMap.put(relProperties, referenceStructuralVariation);
	}

	public int getRefVariationId(RelationshipType RelationshipType)
	{
		Map<String, StructuralVariation> structuralVariationMap = referencesStructuralVariations.get(RelationshipType);
		return structuralVariationMap.keySet().size() + 1;
	}

	public void addReferenceToCount(String name)
	{
		String key = name;
		Integer count = referencesCount.get(key);
		
		if (count == null)
			count = 0;
		
		referencesCount.put(key, count + 1);
	}

	public int getReferenceCount(String name, String refsTo)
	{
		String key = name;
		Integer count = referencesCount.get(key);

		if (count == null) {
			referencesCount.put(key, 1);
			return 1;
		}
		
		return count; 
		
	}

}
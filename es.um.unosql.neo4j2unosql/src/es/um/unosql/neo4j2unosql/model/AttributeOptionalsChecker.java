package es.um.unosql.neo4j2unosql.model;

import static es.um.unosql.neo4j2unosql.constants.Constants.ARRAY;
import static es.um.unosql.neo4j2unosql.constants.Constants.EMPTY;

import org.eclipse.emf.common.util.EList;

import es.um.unosql.neo4j2unosql.model.repository.NoSQLModelRepository;
import es.um.unosql.neo4j2unosql.utils.MapCounter;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.uNoSQLSchema.PTuple;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.Feature;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.DataType;

public class AttributeOptionalsChecker
{
	private NoSQLModelRepository modelRepository;

	public AttributeOptionalsChecker(NoSQLModelRepository modelRepository)
	{
		this.modelRepository = modelRepository;
	}

	public void processOptionals()
	{
		uNoSQLSchema noSQLSchema = modelRepository.getNoSQLSchema();
		
		for (EntityType entity : noSQLSchema.getEntities())
		{
			processEntityPrimitiveTypeOptinals(entity);
		}
	}

	private void processEntityPrimitiveTypeOptinals(EntityType entity)
	{
		MapCounter<String> mapCounter = new MapCounter<String>();
	
		attributesCounts(entity, mapCounter);
		updateAttributesOptionals(entity, mapCounter);
	}

	private void attributesCounts(EntityType entity, MapCounter<String> mapCounter)
	{
		for (StructuralVariation structuralVariation : entity.getVariations())
		{
			for (Feature feature : structuralVariation.getFeatures())
			{
				attributeCount(entity, mapCounter, feature);
			}
		}
	}

	private void attributeCount(EntityType entity, MapCounter<String> mapCounter, Feature feature)
	{
		if (feature instanceof Attribute)
		{
			mapCounter.put(attributeAsString((Attribute) feature));
		}
		else if (feature instanceof Reference)
		{
			mapCounter.put(referenceAsString(entity, (Reference) feature));
		}
	}

	private void updateAttributesOptionals(EntityType entity, MapCounter<String> mapCounter)
	{
		EList<StructuralVariation> variations = entity.getVariations();
		int variationsSize = variations.size();
		
		for (StructuralVariation structuralVariation : entity.getVariations())
		{
			for (Feature feature : structuralVariation.getFeatures())
			{
				attributeUpdate(mapCounter, variationsSize, entity, feature);
			}
		}
	}

	private void attributeUpdate(MapCounter<String> mapCounter, int variationsSize, EntityType entity, Feature feature)
	{
		int attributeCount = variationsSize;
		if (feature instanceof Attribute)
		{
			Attribute attribute = (Attribute) feature;
			attributeCount = mapCounter.get(attributeAsString(attribute));
			
			if (attributeCount < variationsSize)
			{
				attribute.setOptional(true);
			}
		}
		
	}

	private String attributeAsString(Attribute attribute)
	{
		String attributeAsString = attribute.getName() + ":" + getStringType(attribute.getType());
		return attributeAsString.toLowerCase();
	}

	private String referenceAsString(EntityType entity, Reference reference)
	{
		String attributeAsString = entity.getName() + "-[" + reference.getName() + ":" + reference.getIsFeaturedBy().get(0).getVariationId() + "]->" + reference.getRefsTo().getName();
		return attributeAsString.toLowerCase();
	}
	
	private String getStringType(DataType type)
	{
		if (type instanceof PrimitiveType)
		{
			return ((PrimitiveType) type).getName();
		}
		else if (type instanceof PTuple)
		{
			return getStringArrayType((PTuple) type);
		}

		return EMPTY;
	}

	private String getStringArrayType(PTuple pTuple)
	{
		if (pTuple.getElements().size() > 0)
		{
			return ARRAY + getStringType(pTuple.getElements().get(0));	// Only 1 DataType in Neo4j Array.
		}
		else 
		{
			return ARRAY;
		}
	}

}

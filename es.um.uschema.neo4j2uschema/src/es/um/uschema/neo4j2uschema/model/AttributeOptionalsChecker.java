package es.um.uschema.neo4j2uschema.model;

import static es.um.uschema.neo4j2uschema.constants.Constants.ARRAY;
import static es.um.uschema.neo4j2uschema.constants.Constants.EMPTY;

import org.eclipse.emf.common.util.EList;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.neo4j2uschema.model.repository.UModelRepository;
import es.um.uschema.neo4j2uschema.utils.MapCounter;
import es.um.uschema.USchema.PTuple;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.DataType;

public class AttributeOptionalsChecker
{
	private UModelRepository modelRepository;

	public AttributeOptionalsChecker(UModelRepository modelRepository)
	{
		this.modelRepository = modelRepository;
	}

	public void processOptionals()
	{
	  USchema uSchema = modelRepository.getUSchema();
		
		for (EntityType entity : uSchema.getEntities())
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

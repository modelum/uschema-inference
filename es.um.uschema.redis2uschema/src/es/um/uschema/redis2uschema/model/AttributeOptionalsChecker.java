package es.um.uschema.redis2uschema.model;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.redis2uschema.utils.MapCounter;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.StructuralFeature;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PSet;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.DataType;

public class AttributeOptionalsChecker {

	private USchema uSchema;

	public AttributeOptionalsChecker(USchema uSchema)
	{
		this.uSchema = uSchema;
	}

	public void processOptionals()
	{
		for (EntityType entity : uSchema.getEntities())
			processEntityPrimitiveTypeOptinals(entity);
	}

	private void processEntityPrimitiveTypeOptinals(EntityType entity)
	{
		MapCounter<String> mapCounter = new MapCounter<String>();

		attributesCounts(entity, mapCounter);
		updatestructuralFeatureOptionals(entity, mapCounter);
	}

	private void attributesCounts(EntityType entity, MapCounter<String> mapCounter)
	{
		for (StructuralVariation structuralVariation : entity.getVariations())
		{
			for (StructuralFeature feature : structuralVariation.getStructuralFeatures()) 
				structuralFeatureCount(entity, mapCounter, feature);
		}
	}

	private void structuralFeatureCount(EntityType entity, MapCounter<String> mapCounter, StructuralFeature feature)
	{
		if (feature instanceof Attribute) {
			mapCounter.put(attributeAsString((Attribute) feature));
		} else if (feature instanceof Aggregate) {
			mapCounter.put(aggregateAsString((Aggregate) feature));			
		}
	}

	private void updatestructuralFeatureOptionals(EntityType entity, MapCounter<String> mapCounter)
	{
		List<StructuralVariation> variations = entity.getVariations();
		int variationsSize = variations.size();

		for (StructuralVariation structuralVariation : entity.getVariations()) {
			for (StructuralFeature feature : structuralVariation.getStructuralFeatures()) {
				structuralFeatureUpdate(mapCounter, variationsSize, entity, feature);
			}
		}
	}

	private void structuralFeatureUpdate(MapCounter<String> mapCounter, int variationsSize, EntityType entity, StructuralFeature feature)
	{
		int structuralFeatureCount = 0;
		if (feature instanceof Attribute)
		{
			Attribute attribute = (Attribute) feature;
			structuralFeatureCount = mapCounter.get(attributeAsString(attribute));

			if (structuralFeatureCount == variationsSize)
				attribute.setOptional(false);
		} else if (feature instanceof Aggregate)
		{
			Aggregate aggregate = (Aggregate) feature;
			structuralFeatureCount = mapCounter.get(aggregateAsString(aggregate));

			if (structuralFeatureCount == variationsSize)
				aggregate.setOptional(false);
		}

	}

	private String attributeAsString(Attribute attribute) 
	{
		String attributeAsString = attribute.getName() + ":" + getStringType(attribute.getType());
		return attributeAsString.toLowerCase();
	}

	private String aggregateAsString(Aggregate aggregate)
	{
		String attributeAsString = aggregate.getName() + "<>-->" + aggregate.getAggregates().stream()
				.map(sv -> {
					EObject eContainer = sv.eContainer();
					if (eContainer instanceof EntityType)
					{
						EntityType entityType = (EntityType) eContainer;
						return entityType.getName();
					}
					return "";
				}).collect(Collectors.joining(" , ")); 
		return attributeAsString.toLowerCase();
	}

	private String getStringType(DataType type)
	{
		if (type instanceof PrimitiveType) {
			return ((PrimitiveType) type).getName();
		} else if (type instanceof PList) {
			return getStringListType((PList) type);
		} else if (type instanceof PSet) {
			return getStringListType((PSet) type);
		}

		return "";
	}

	private String getStringListType(PList pList) {
		return "LIST" + getStringType(pList.getElementType());
	}

	private String getStringListType(PSet pSet) {
		return "SET" + getStringType(pSet.getElementType());
	}
}

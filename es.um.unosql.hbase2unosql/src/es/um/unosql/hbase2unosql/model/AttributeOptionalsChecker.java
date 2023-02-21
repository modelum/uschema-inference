package es.um.unosql.hbase2unosql.model;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.StructuralFeature;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PSet;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.DataType;

public class AttributeOptionalsChecker {

	private uNoSQLSchema noSQLSchema;

	public AttributeOptionalsChecker(uNoSQLSchema noSQLSchema)
	{
		this.noSQLSchema = noSQLSchema;
	}

	public void processOptionals()
	{
		for (EntityType entity : noSQLSchema.getEntities())
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

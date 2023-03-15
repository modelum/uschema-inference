package es.um.uschema.neo4j2uschema.model;

import static es.um.uschema.neo4j2uschema.constants.Constants.CLOSING_BRACKET;
import static es.um.uschema.neo4j2uschema.constants.Constants.CLOSING_SQUARE_BRACKETS;
import static es.um.uschema.neo4j2uschema.constants.Constants.COLON;
import static es.um.uschema.neo4j2uschema.constants.Constants.COMMA;
import static es.um.uschema.neo4j2uschema.constants.Constants.OPENING_BRACKET;
import static es.um.uschema.neo4j2uschema.constants.Constants.OPENING_SQUARE_BRACKETS;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.neo4j2uschema.model.repository.UModelRepository;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PMap;
import es.um.uschema.USchema.PSet;
import es.um.uschema.USchema.PTuple;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;

public class IgnoreSimilarReferenceBoundsProcessor
{
	private UModelRepository modelRepository;

	public IgnoreSimilarReferenceBoundsProcessor(UModelRepository modelRepository)
	{
		this.modelRepository = modelRepository;
	}

	/** Treat similar variations with only difference at bounds on the reference 1..1 and 1..* as equals:
		joining them into one variation 1..*.
		Variation with reference 1..1 is removed.
	*/
	public void joinVariationsIgnoringBounds()
	{
		// Search for similar variations with references 1..1 and 1..* and update upperBound to *
	  USchema uSchema = modelRepository.getUSchema();
	  uSchema.getEntities().forEach(e -> {
			copySimilarReferencesIgnoringBounds(e);
			removeDuplicates(e);
			updateVariationIds(e);
		});
	}

	private void updateVariationIds(EntityType e)
	{
		int variationId = 1;
		for (StructuralVariation variation : e.getVariations())
		{
			variation.setVariationId(variationId);
			variationId++;
		}
	}

	private void removeDuplicates(EntityType e)
	{
		final SortedMap<String, List<StructuralVariation>> similarVariations = generateSimilarVariationsMap(e);
		similarVariations.keySet().forEach(k -> {
			List<StructuralVariation> variations = similarVariations.get(k);
			if (variations.size() > 1)
			{
				StructuralVariation variation = variations.get(0);
				variations.remove(variation);
				variations.forEach(v -> {
					variation.setCount(variation.getCount() + v.getCount());
					e.getVariations().remove(v);
				});
			}
		});
	}

	private void copySimilarReferencesIgnoringBounds(EntityType e)
	{
		final SortedMap<String, List<StructuralVariation>> similarVariations = generateSimilarVariationsMap(e);
		similarVariations.keySet().forEach(k -> {
			List<StructuralVariation> variations = similarVariations.get(k);
			variations.forEach(v1 -> 
				variations.forEach(v2 -> {
					if (v1 != v2)
						compareVariations(v1, v2);
				})
			);
		});
	}

	private SortedMap<String, List<StructuralVariation>> generateSimilarVariationsMap(EntityType e)
	{
		SortedMap<String, List<StructuralVariation>> similarVariations = new TreeMap<String, List<StructuralVariation>>();
		e.getVariations().forEach(ev -> {
			final List<String> properties = new LinkedList<String>();
			ev.getFeatures().stream().forEach(p -> {
				putPropertyKeyOnList(properties, p);
			});
			
			String key = properties.stream().sorted().collect(Collectors.joining(COMMA));
			putOnMap(key, ev, similarVariations);
		});
		
		return similarVariations;
	}

	private void compareVariations(StructuralVariation variation1, StructuralVariation variation2)
	{
		List<Reference> references1 = variation1.getFeatures().stream()
			.filter(Reference.class::isInstance).map(Reference.class::cast)
			.collect(Collectors.toList());
		
		List<Reference> references2 = variation2.getFeatures().stream()
			.filter(Reference.class::isInstance).map(Reference.class::cast)
			.collect(Collectors.toList());
		
		references1.forEach(r1 -> 
			references2.forEach(r2 -> {
				if (r1 != r2)
					compareReferences(r1, r2);
			})
		);
	}

	private void compareReferences(Reference r1, Reference r2)
	{
		String referenceType1 = getReferenceRepresentation(r1);
		String referenceType2 = getReferenceRepresentation(r2);
		
		if (referenceType1.equals(referenceType2))
		{
			copyFeaturesInBothReferences(r1, r2);
			r1.setUpperBound(Math.min(r1.getUpperBound(), r2.getUpperBound()));
			r2.setUpperBound(Math.min(r1.getUpperBound(), r2.getUpperBound()));
		}
	}

	private void copyFeaturesInBothReferences(Reference r1, Reference r2)
	{
		Set<StructuralVariation> featuresSet = new HashSet<StructuralVariation>(r1.getIsFeaturedBy());
		featuresSet.addAll(r2.getIsFeaturedBy());
		
		r1.getIsFeaturedBy().clear();
		r1.getIsFeaturedBy().addAll(featuresSet);
		
		r2.getIsFeaturedBy().clear();
		r2.getIsFeaturedBy().addAll(featuresSet);
	}

	private void putOnMap(String key, StructuralVariation ev, SortedMap<String, List<StructuralVariation>> similarVariations)
	{
		List<StructuralVariation> variationList = similarVariations.get(key);
		if (variationList == null)
		{
			variationList = new LinkedList<StructuralVariation>();
			similarVariations.put(key, variationList);
		}
		variationList.add(ev);
	}

	private void putPropertyKeyOnList(final List<String> properties, Feature f)
	{
		String propertyName = f.getName();
		if (f instanceof Attribute)
		{
			Attribute attribute = (Attribute) f;
			String propertyType = getAttributeRepresentation(attribute);
			properties.add(propertyName + COLON + propertyType);
		} else if (f instanceof Reference)
		{
			Reference reference = (Reference) f;
			String propertyType = getReferenceRepresentation(reference);
			properties.add(propertyName + COLON + propertyType);
		}
	}

	private String getAttributeRepresentation(Attribute attribute)
	{
		return attribute.getName() + getTypeRepresentation(attribute.getType());
	}

	private String getReferenceRepresentation(Reference reference)
	{
		return reference.getName() + "->" + reference.getRefsTo().getName();
	}

	private String getTypeRepresentation(DataType type)
	{
		if (type instanceof PrimitiveType)
			return ((PrimitiveType) type).getName();
		else if (type instanceof PList)
			return getTypeRepresentation(((PList) type).getElementType()) + OPENING_SQUARE_BRACKETS + CLOSING_SQUARE_BRACKETS;
		else if (type instanceof PSet)
			return getTypeRepresentation(((PSet) type).getElementType()) + OPENING_BRACKET + CLOSING_BRACKET;
		else if (type instanceof PTuple)
			return OPENING_SQUARE_BRACKETS + ((PTuple) type).getElements().stream().map(e -> getTypeRepresentation(e)).collect(Collectors.joining(COMMA)) + CLOSING_SQUARE_BRACKETS;
		else if (type instanceof PMap)
		{
			PMap pMap = (PMap) type;
			return OPENING_BRACKET + getTypeRepresentation(pMap.getKeyType()) + COLON + getTypeRepresentation(pMap.getValueType()) + CLOSING_BRACKET;
		} 
		
		return "";
	}
}

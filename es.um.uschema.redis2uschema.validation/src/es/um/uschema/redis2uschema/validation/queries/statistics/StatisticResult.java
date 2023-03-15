package es.um.uschema.redis2uschema.validation.queries.statistics;

import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.EMPTY;
import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.TAB;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PSet;
import es.um.uschema.USchema.PTuple;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;

public class StatisticResult {
	private StructuralVariation structuralVariation;

	private long countExpected;
	private long countObtained;
	private List<Attribute> attributes;
	private List<Reference> references;
	private List<Aggregate> aggregations;

	public StatisticResult(StructuralVariation structuralVariation, int resultNumber) {
		this.structuralVariation = structuralVariation;
		this.countObtained = resultNumber;
		this.countExpected = structuralVariation.getCount();
		this.attributes = properties(structuralVariation);
		this.references = structuralVariation.getFeatures().stream().filter(Reference.class::isInstance)
				.map(Reference.class::cast).collect(Collectors.toList());
		this.aggregations = structuralVariation.getFeatures().stream().filter(Aggregate.class::isInstance)
				.map(Aggregate.class::cast).collect(Collectors.toList());
	}

	private List<Attribute> properties(StructuralVariation structuralVariation) {
		return structuralVariation.getFeatures().stream().filter(Attribute.class::isInstance).map(Attribute.class::cast)
				.collect(Collectors.toList());
	}

	public String getStatisticsAsString() {
		String result = EMPTY;
		long expected = countExpected;

		result += structuralVariation.getContainer().getName() + TAB + TAB;
		result += structuralVariation.getVariationId() + TAB + TAB;
		result += (countObtained == expected ? "PASSED" : "FAILED") + TAB;
		result += countObtained + TAB + TAB;
		result += expected + TAB + TAB;
		result += "Properties: [" + String.join("; ", getFeatures(attributes)) + " , "
				+ String.join("; ", getAggregations(aggregations)) + "]" + TAB;
		result += "References: [" + String.join("; ", getReferences()) + "]" + TAB;

		return result;
	}

	private List<String> getAggregations(List<Aggregate> aggregations) {
		List<String> properties = new LinkedList<String>();
		for (Aggregate aggregation : aggregations) {
			String propertyString = getAggregateAsString(aggregation);
			properties.add(propertyString);
		}
		return properties;
	}

	private List<String> getFeatures(List<Attribute> attributes) {
		List<String> properties = new LinkedList<String>();
		for (Attribute attribute : attributes) {
			String propertyString = getAttributeAsString(attribute);
			properties.add(propertyString);
		}
		return properties;
	}

	private String getAggregateAsString(Aggregate aggregate) {
		String name = aggregate.getName();
		String attributes = aggregate.getAggregates().stream()
				.flatMap(a -> a.getStructuralFeatures().stream().filter(Attribute.class::isInstance)
						.map(Attribute.class::cast).map(at -> getAttributeAsString(at)).collect(Collectors.toList())
						.stream())
				.collect(Collectors.joining("; "));

		return name + " : [" + attributes + "]";
	}

	private String getAttributeAsString(Attribute attribute) {
		String name = attribute.getName();
		DataType type = attribute.getType();
		String typeName = EMPTY;

		if (type instanceof PrimitiveType) {
			PrimitiveType primitiveType = (PrimitiveType) type;
			typeName = primitiveType.getName();
		} else if (type instanceof PTuple) {
			typeName = getPTupleAsString((PTuple) type, typeName);
		} else if (type instanceof PList) {
			typeName = getPListAsString((PList) type, typeName);
		} else if (type instanceof PSet) {
			typeName = getPSetAsString((PSet) type, typeName);
		}

		return name + ":" + typeName;
	}

	private String getPTupleAsString(PTuple type, String typeName) {
		if (type.getElements().size() > 0) {
			DataType pTupleType = type.getElements().get(0);
			if (pTupleType instanceof PrimitiveType) {
				typeName = "Array[" + ((PrimitiveType) pTupleType).getName() + "]";
			}

		}
		return typeName;
	}

	private String getPListAsString(PList type, String typeName) {
		DataType pTupleType = type.getElementType();
		if (pTupleType instanceof PrimitiveType) {
			typeName = "List[" + ((PrimitiveType) pTupleType).getName() + "]";
		}

		return typeName;
	}

	private String getPSetAsString(PSet type, String typeName) {
		DataType pTupleType = type.getElementType();
		if (pTupleType instanceof PrimitiveType) {
			typeName = "Set[" + ((PrimitiveType) pTupleType).getName() + "]";
		}

		return typeName;
	}

	private String getReferences() {
		List<String> referencesStrings = new LinkedList<String>();
		for (Reference reference : references) {
			String referenceString = reference.getAttributes().stream().map(a -> a.getName()).collect(Collectors.joining(","));
			referenceString += "-->" + reference.getRefsTo().getName();
			if (reference.getIsFeaturedBy().size() > 0)
				referenceString += "{" + getFeatures(properties(reference.getIsFeaturedBy().get(0))) + "}";
			referencesStrings.add(referenceString);
		}

		return String.join(":", referencesStrings);
	}

	public StructuralVariation getStructuralVariation() {
		return structuralVariation;
	}

	public void addCount() {
		countObtained++;
	}

}

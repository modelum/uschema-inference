package es.um.uschema.neo4j.queries.statistics;

import static es.um.uschema.neo4j.queries.constants.Constants.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.sql.Row;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.PTuple;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.DataType;

public class StatisticResult
{
	private StructuralVariation structuralVariation;
	private String query;
	private LinkedList<Row> resultRows;
	
	private long countExpected;
	private long countObtained;
	private List<Attribute> attributes;
	private List<Reference> references;

	public StatisticResult(StructuralVariation structuralVariation, List<Row> resultRows, String query)
	{
		this.structuralVariation = structuralVariation;
		this.resultRows = new LinkedList<Row>(resultRows);
		this.query = query;
		
		this.countObtained = resultRows.size();
		this.countExpected = structuralVariation.getCount();
		this.attributes = properties(structuralVariation);
		this.references = structuralVariation.getFeatures().stream().filter(Reference.class::isInstance).map(Reference.class::cast).collect(Collectors.toList());
	}

	private List<Attribute> properties(StructuralVariation structuralVariation)
	{
		return structuralVariation.getFeatures().stream().filter(Attribute.class::isInstance).map(Attribute.class::cast).collect(Collectors.toList());
	}

	public String getStatisticsAsString(boolean findOne)
	{
		String result = EMPTY;
		long expected = findOne ? 1 : countExpected;
		
		result += (countObtained == expected ? "PASSED" : "FAILED") + TAB;
		result += countObtained + TAB;
		result += expected + TAB;
		result += "Properties: [" + String.join("; ", getFeatures(attributes)) + "]" + TAB;
		result += "References: [" + String.join("; ", getReferences()) + "]" + TAB;
		result += query;
		
		return result;
	}

	private List<String> getFeatures(List<Attribute> attributes)
	{
		List<String> properties = new LinkedList<String>();
		for (Attribute attribute : attributes)
		{
			String propertyString = getAttributeAsString(attribute);
			properties.add(propertyString);
		}
		return properties;
	}

	private String getAttributeAsString(Attribute attribute)
	{
		String name = attribute.getName();
		DataType type = attribute.getType();
		String typeName = EMPTY;
		
		if (type instanceof PrimitiveType)
		{
			PrimitiveType primitiveType = (PrimitiveType) type;
			typeName = primitiveType.getName();
		}
		else if (type instanceof PTuple)
		{
			typeName = getPTupleAsString(type, typeName);
		}
		
		return name + ":" + typeName;
	}

	private String getPTupleAsString(DataType type, String typeName)
	{
		PTuple pTuple = (PTuple) type;
		if (pTuple.getElements().size() > 0)
		{
			DataType pTupleType = pTuple.getElements().get(0);
			if (pTupleType instanceof PrimitiveType)
			{
				typeName = "Array[" + ((PrimitiveType)pTupleType).getName() + "]";
			}
			
		}
		return typeName;
	}

	private String getReferences()
	{
		List<String> referencesStrings = new LinkedList<String>();
		for (Reference reference : references)
		{
			String referenceString = reference.getName();
			referenceString += "-->" + reference.getRefsTo().getName();
			referenceString += "{" + getFeatures(properties(reference.getIsFeaturedBy().get(0))) + "}";
			referencesStrings.add(referenceString);
		}
		
		return String.join(":", referencesStrings);
	}
	
	public StructuralVariation getStructuralVariation()
	{
		return structuralVariation;
	}

	public String getQuery()
	{
		return query;
	}

	public LinkedList<Row> getResultRows()
	{
		return resultRows;
	}
	
}

package es.um.unosql.neo4j.queries.builder;

import static es.um.unosql.neo4j.queries.constants.Constants.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.SchemaType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class PropertiesQueryBuilder
{

	public String whereClause(SchemaType SchemaType, StructuralVariation structuralVariation, String variableName)
	{
		String properties = propertiesWhereClause(structuralVariation, variableName); // EXITS (r.PROPERTY_NAME)
		if (properties.isEmpty())
		{
			return EMPTY;
		}
		
		String notExpectedProperties = notExpectedPropertiesWhereClause(SchemaType, 
				structuralVariation, variableName); // NOT EXITS (r.PROPERTY_NAME)
		if (! notExpectedProperties.isEmpty()) 
		{
			properties += "AND " + notExpectedProperties;
		}
		
		return properties;
		
	}
	
	public String whereClause(List<StructuralVariation> features, String variableName)
	{
		Set<String> intersection = featuresIntersection(features);
		String properties = propertiesWhereClause(variableName, intersection);
		if (properties.isEmpty())
		{
			return EMPTY;
		}
		
		return properties;
	}

	private Set<String> featuresIntersection(List<StructuralVariation> features)
	{
		Set<String> intersection = null;
		for (StructuralVariation structuralVariation : features)
		{
			Set<String> properties = structuralVariation.getFeatures()
				.stream()
					.map(p -> p.getName())
					.collect(Collectors.toSet());
			
			if (intersection == null) {
				intersection = properties;
			} else {
				intersection = intersection(intersection, properties);
			}
		}
		
		return intersection;
	}
	
	public static Set<String> intersection(Set<String> a, Set<String> b) 
	{
	    if (a.size() > b.size())
	        return intersection(b, a);

	    Set<String> results = new HashSet<String>();
	    for (String element : a) 
	    {
	        if (b.contains(element)) 
	            results.add(element);
	    }

	    return results;
	}

	private String propertiesWhereClause(StructuralVariation structuralVariation, String variableName)
	{
		List<String> structuralVariationProperties = structuralVariationProperties(structuralVariation);
		
		return propertiesWhereClause(variableName, structuralVariationProperties);
	}

	private String propertiesWhereClause(String variableName, Collection<String> structuralVariationProperties)
	{
		String result = "";
		for (String property : structuralVariationProperties)
		{
			if (!_ID.equals(property))
			{
				if (result.isEmpty())
				{
					result += " EXISTS(" + variableName + "." + property + ") ";
				}
				else
				{
					result += " AND EXISTS(" + variableName + "." + property + ") ";
				}
			}
		}
		
		return result;
	}

	private List<String> structuralVariationProperties(StructuralVariation structuralVariation)
	{
		List<String> properties = 
			structuralVariation.getFeatures()
				.stream()
				.filter(Attribute.class::isInstance)
				.map(p -> p.getName())
				.collect(Collectors.toList());
	
		return properties;
	}

	private String notExpectedPropertiesWhereClause(SchemaType SchemaType,
			StructuralVariation structuralVariation, String variableName)
	{
		List<String> structuralVariationProperties = structuralVariationProperties(structuralVariation);
		Set<String> notExpectedProperties = notExpectedProperties(SchemaType,
				structuralVariationProperties);

		String result = "";
		for (String property : notExpectedProperties)
		{
			if (!_ID.equals(property))
			{
				if (result.isEmpty())
				{
					result += " NOT EXISTS(" + variableName + "." + property + ") ";
				}
				else
				{
					result += " AND NOT EXISTS(" + variableName + "." + property + ") ";
				}
			}
		}
		
		return result;
	}

	private Set<String> notExpectedProperties(SchemaType SchemaType, 
			List<String> structuralVariationProperties)
	{
		Set<String> notExpectedProperties = allPropertiesFromStructuralVariationBrothers(SchemaType);
		
		// Brothers properties less StructuralVariation properties
		for (String structuralVariationProperty : structuralVariationProperties)
		{
			notExpectedProperties.remove(structuralVariationProperty);
		}
		
		return notExpectedProperties;
	}

	private Set<String> allPropertiesFromStructuralVariationBrothers(SchemaType SchemaType)
	{
		Set<String> allStructuralVariationsProperties = 
			SchemaType.getVariations()	
				.stream()
				.flatMap(sv -> sv.getFeatures().stream())
				.filter(Attribute.class::isInstance)
				.map(p -> p.getName())
				.collect(Collectors.toSet());
		
		return allStructuralVariationsProperties;
	}

}
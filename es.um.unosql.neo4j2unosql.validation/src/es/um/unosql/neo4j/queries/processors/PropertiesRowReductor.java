package es.um.unosql.neo4j.queries.processors;

import static es.um.unosql.neo4j.queries.constants.Constants.LIST_TYPE;
import static es.um.unosql.neo4j.queries.constants.Constants.NODE_PROPERTIES;
import static es.um.unosql.neo4j.queries.constants.Constants.NODE_REL_PROPERTIES;
import static es.um.unosql.neo4j.queries.constants.Constants.NODE_REL_TARGET_NODE_TYPE;
import static es.um.unosql.neo4j.queries.constants.Constants.NODE_REL_TYPE;
import static es.um.unosql.neo4j.queries.constants.Constants.REL_COLUMNS_NUMBER;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.spark.sql.Row;

import es.um.unosql.neo4j.queries.utils.StringNameUtils;
import es.um.unosql.neo4j.queries.utils.TypeUtils;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import scala.collection.JavaConverters;

public class PropertiesRowReductor
{
	private StructuralVariation mainStructuralVariation;

	public PropertiesRowReductor(StructuralVariation structuralVariation)
	{
		this.mainStructuralVariation = structuralVariation;
	}

	private HashMap<String,Class<?>> propertiesMap(StructuralVariation structuralVariation)
	{
		final HashMap<String,Class<?>> propertiesMap = new HashMap<String, Class<?>>();
		
		structuralVariation.getFeatures()
			.stream()
			.filter(Attribute.class::isInstance).map(Attribute.class::cast).
			forEach(a -> {
				DataType type = a.getType();
				if (type instanceof PrimitiveType)
				{
					propertiesMap.put(a.getName(), TypeUtils.getClassType((PrimitiveType) type));
				}
				else if (type instanceof PList)
				{
					processList(propertiesMap, a, (PList) type);
				}
			});
		
		return propertiesMap;
	}

	private void processList(HashMap<String, Class<?>> propertiesMap, Attribute a, PList pList)
	{
		DataType dataType = pList.getElementType();
		if (dataType instanceof PrimitiveType)
		{
			propertiesMap.put(a.getName(), TypeUtils.getClassType((PrimitiveType) dataType));
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Row> reduceVariationRows(List<Row> rows, boolean findOne)
	{
		List<Row> matchingRows = new LinkedList<Row>();
		for (Row row : rows)
		{
			Map<String, Object> structuralVariationProperties = (Map<String, Object>) JavaConverters.mapAsJavaMapConverter((scala.collection.Map<String, Object>) row.get(NODE_PROPERTIES)).asJava();

			if (arePropertiesMatching(mainStructuralVariation, structuralVariationProperties))
			{
				if (row.length() == REL_COLUMNS_NUMBER)	     // If it's a ReferenceClass
				{
					matchingRows.add(row);	
					if (findOne) return matchingRows;

				} else if (areReferencesMatching(row))   // If it's an Entityclass
				{
					matchingRows.add(row);
					if (findOne) return matchingRows;
				}
			}
		}
		
		return matchingRows;
	}

	private boolean areReferencesMatching(Row row)
	{
		Object[] referencesPropertiesArray = (Object[]) row.get(NODE_REL_PROPERTIES);
		List<Map<String, Object>> referencesProperties =  Stream.of(referencesPropertiesArray).map(Map.class::cast).collect(Collectors.toList());; 
		
		Object[] referenceTypeArray = (Object[]) row.get(NODE_REL_TYPE);
		List<String> referenceType = Stream.of(referenceTypeArray).map(String.class::cast).collect(Collectors.toList());
		Object[] targetLabelsArray = (Object[]) row.get(NODE_REL_TARGET_NODE_TYPE);
		List<Object> targetLabels = Stream.of(targetLabelsArray).collect(Collectors.toList());
		
		
		if (referencesProperties.isEmpty() || referenceType.isEmpty() || targetLabels.isEmpty())
			return true;
		
		return processReferences(referencesProperties, referenceType, targetLabels);
	}

	private boolean processReferences(List<Map<String, Object>> referenceProperties, List<String> referenceType,
			List<Object> targetLabels)
	{
		Iterator<Map<String, Object>> refPropertiesIt = referenceProperties.iterator();
		Iterator<String> refTypeIt = referenceType.iterator();
		Iterator<Object> targetLabelsIt = targetLabels.iterator();
		
		return checkReferences(refPropertiesIt, refTypeIt, targetLabelsIt);
	}

	private boolean checkReferences(Iterator<Map<String, Object>> refPropertiesIt, Iterator<String> refTypeIt,
			Iterator<Object> targetLabelsIt)
	{
		while (refPropertiesIt.hasNext() && refTypeIt.hasNext() && targetLabelsIt.hasNext())
		{
			String type = refTypeIt.next();
			String targetLabel = StringNameUtils.targetLabels(targetLabelsIt.next());
			Map<String, Object> properties = refPropertiesIt.next();
			
			List<Reference> selectedReferences = searchRelationshipVariation(type, targetLabel);
			if (!oneReferenceMatching(properties, selectedReferences))
				return false;
		}
		
		return true;
	}

	private boolean oneReferenceMatching(Map<String, Object> properties, List<Reference> selectedReferences)
	{
		return selectedReferences.stream()
				.flatMap(r -> r.getIsFeaturedBy().stream())
				.anyMatch(f -> arePropertiesMatching(f,properties));
	}

	private List<Reference> searchRelationshipVariation(String type, String targetLabel)
	{
		return getReferences().stream().filter(ref -> ref.getName().equals(type) && ref.getRefsTo().getName().equals(targetLabel))
				.collect(Collectors.toList());
	}


	private boolean arePropertiesMatching(StructuralVariation structuralVariation, Map<String, Object> properties)
	{
		HashMap<String,Class<?>> propertiesMap = propertiesMap(structuralVariation);
		
		for (String propertyName : propertiesMap.keySet())
		{
			Object expectedPropertyType = propertiesMap.get(propertyName);
			Object propertyObtained = properties.get(propertyName);

			if (propertyObtained == null || !isTypeMatching(expectedPropertyType, propertyObtained))
			{
				return false;
			}
		}
		
		return propertiesMap.keySet().size() == properties.keySet().size(); // All but _id
	}

	@SuppressWarnings("rawtypes")
	private boolean isTypeMatching(Object expectedPropertyType, Object propertyObtained)
	{
		if (propertyObtained instanceof List)
		{
			if (((List) propertyObtained).isEmpty() && expectedPropertyType == null)
			{
				return true;
			}
			return isArrayTypeMatching(propertyObtained, expectedPropertyType);
		} 
		else
		{
			return expectedPropertyType.equals(propertyObtained.getClass());
		}
	}

	private boolean isArrayTypeMatching(Object propertyObtained, Object expectedPropertyType)
	{
		try
		{
			Class<?> listClass = Class.forName(LIST_TYPE);
			if (propertyObtained.getClass().equals(listClass)) 
			{
				return isArrayTypeMatching(propertyObtained, expectedPropertyType, listClass);
			} 
		} catch (ClassNotFoundException e) { /* Will never happen*/ }
		
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean isArrayTypeMatching(Object propertyObtained, Object expectedPropertyType, Class<?> listClass)
	{
		List<Object> cast = (List<Object>) listClass.cast(propertyObtained);
		if (cast.size() > 0) 
		{
			Object object = cast.get(0);
			if (object.getClass().equals(expectedPropertyType)) 
			{
				return true;
			}
		} 
		
		return false;
	}

	private List<Reference> getReferences()
	{
		List<Reference> references = mainStructuralVariation.getFeatures()
			.stream()
			.filter(Reference.class::isInstance)
			.map(Reference.class::cast)
			.collect(Collectors.toList());
		
		return references;
	}
}
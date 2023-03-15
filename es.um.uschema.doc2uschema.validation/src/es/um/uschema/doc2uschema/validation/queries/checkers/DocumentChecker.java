package es.um.uschema.doc2uschema.validation.queries.checkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.eclipse.emf.common.util.EList;

import com.mongodb.client.FindIterable;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.StructuralFeature;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.doc2uschema.validation.queries.utils.TypeUtils;

public class DocumentChecker
{
	private StructuralVariation mainStructuralVariation;
	private Map<StructuralVariation, Integer> aggregatedVariationsCounter;
	private LinkedList<Document> documents;

	public DocumentChecker(StructuralVariation structuralVariation, Map<StructuralVariation, Integer> aggregatedVariationsCounter) {
		this.mainStructuralVariation = structuralVariation;
		this.aggregatedVariationsCounter = aggregatedVariationsCounter;
//		this.propertiesMap = createPropertiesMap(mainStructuralVariation);
	}
	
	private HashMap<String,Class<?>> createPropertiesMap(StructuralVariation structuralVariation) {
		final HashMap<String,Class<?>> propertiesMap = new HashMap<String, Class<?>>();
		
		structuralVariation.getFeatures().stream()
			.filter(Attribute.class::isInstance).map(Attribute.class::cast).forEach(a -> {
				DataType type = a.getType();
				if (type instanceof PrimitiveType) {
					propertiesMap.put(a.getName(), TypeUtils.getClassType((PrimitiveType) type));
				} else if (type instanceof PList) {
					processList(propertiesMap, a, (PList) type);
				} 
			});
		
		structuralVariation.getFeatures().stream()
			.filter(Aggregate.class::isInstance).map(Aggregate.class::cast).forEach(a -> {
				processAggregate(propertiesMap, a);
			});
		
		return propertiesMap;
	}

	private void processAggregate(final HashMap<String, Class<?>> propertiesMap, Aggregate a) {
		try {
			propertiesMap.put(a.getName(), Class.forName("java.util.ArrayList"/*Constants.DOCUMENT_FULL_NAME*/));
		} catch (ClassNotFoundException e) { }
	}
	
	public List<Document> reduceVariationDocuments(FindIterable<Document> resultCursor) {
		documents = new LinkedList<Document>();
		
		HashMap<String, Class<?>> propertiesMap = createPropertiesMap(mainStructuralVariation);
		resultCursor.forEach(d -> { 
			if (d.keySet().size() == mainStructuralVariation.getStructuralFeatures().size() &&
					arePropertiesMatching(d, propertiesMap)) 
				documents.add(d); 
		});

		return documents;
	}

	private void processList(HashMap<String, Class<?>> propertiesMap, Attribute a, PList pList) {
		DataType dataType = pList.getElementType();
		if (dataType instanceof PrimitiveType) {
			propertiesMap.put(a.getName(), TypeUtils.getClassType((PrimitiveType) dataType));
		}
	}


	private boolean arePropertiesMatching(Document d, HashMap<String, Class<?>> propertiesMap) {
		Map<StructuralVariation, Integer> saved = aggregatedVariationsCounter;
		aggregatedVariationsCounter = new HashMap<>(saved);
		
		for (String propertyName : propertiesMap.keySet()) {
			Object expectedPropertyType = propertiesMap.get(propertyName);
			Object propertyObtained = d.get(propertyName);

			if (propertyName.equals("_id") && propertyObtained != null) {
				
			} else if (propertyObtained == null || !isTypeMatching(propertyName, expectedPropertyType, propertyObtained)) {
				aggregatedVariationsCounter = saved;
				return false;
			}
		}
		
		if (propertiesMap.keySet().size() == d.keySet().size()) {
			saved.putAll(aggregatedVariationsCounter);
			aggregatedVariationsCounter = saved;
			
			return true;
		} else {
			return false;
		}
	}

	private boolean isTypeMatching(String propertyName, Object expectedPropertyType, Object propertyObtained) {
		if (propertyObtained instanceof List) {
			if (((List<?>) propertyObtained).isEmpty() && expectedPropertyType == null) {
				return true;
			} else if (propertyObtained instanceof ArrayList) {
				
				ArrayList<?> arrayList = (ArrayList<?>) propertyObtained;
				
				for (Object aggregated: arrayList) {
					if (aggregated instanceof Document) {
						Document aggregatedDocument = (Document) aggregated;
						Optional<Aggregate> findFirst = mainStructuralVariation.getStructuralFeatures().stream()
								.filter(Aggregate.class::isInstance).map(Aggregate.class::cast)
								.filter(sf -> sf.getName().equals(propertyName)).findFirst();
						if (findFirst.isPresent()) {
							Aggregate aggregate = findFirst.get();
							EList<StructuralVariation> aggregates = aggregate.getAggregates();
							for (StructuralVariation structuralVariation : aggregates) {
								HashMap<String, Class<?>> aggregatePropertiesMap = createPropertiesMap(structuralVariation);
								if (aggregatedDocument.keySet().size() == structuralVariation.getStructuralFeatures().size() &&
										arePropertiesMatching(aggregatedDocument, aggregatePropertiesMap)) {
									
									if (!aggregatedVariationsCounter.containsKey(structuralVariation)) {
										aggregatedVariationsCounter.put(structuralVariation, 0);
									}
									Integer oldValue = aggregatedVariationsCounter.get(structuralVariation);
									aggregatedVariationsCounter.put(structuralVariation, oldValue + 1);
									
									return true;
								}
							}
						}
					} else if (aggregated instanceof Integer) {
						return expectedPropertyType.equals(Integer.class);
					} else if (aggregated instanceof Long) {
						return expectedPropertyType.equals(Long.class);
					} else if (aggregated instanceof String) {
						return expectedPropertyType.equals(String.class);
					} else if (aggregated instanceof Float) {
						return expectedPropertyType.equals(Float.class);
					} else if (aggregated instanceof Double) {
						return expectedPropertyType.equals(Double.class);
					}
				}
			} 
			return isArrayTypeMatching(propertyObtained, expectedPropertyType);
		} else if (propertyObtained instanceof Document) {
			Document d = (Document) propertyObtained;
			Optional<StructuralFeature> insideStructuralVariation = mainStructuralVariation.getStructuralFeatures().stream().filter(sf -> propertyName.equals(sf.getName())).findFirst();
			if (insideStructuralVariation.isPresent()) {
				StructuralFeature structuralFeature = insideStructuralVariation.get();
				if (structuralFeature instanceof Aggregate) {
					Aggregate aggregate = (Aggregate) structuralFeature;
					for (StructuralVariation structuralVariation : aggregate.getAggregates()) {
						HashMap<String,Class<?>> propertiesMap = createPropertiesMap(structuralVariation);
						if (d.keySet().size() == mainStructuralVariation.getStructuralFeatures().size() &&
								arePropertiesMatching(d, propertiesMap)) 
							documents.add(d); 
						return true;
					}
				}
			
			}
		} else if (expectedPropertyType != null && propertyObtained != null) {
			return expectedPropertyType.equals(propertyObtained.getClass());
		} else {
			return false;
		}
		return false;
	}

	private boolean isArrayTypeMatching(Object propertyObtained, Object expectedPropertyType) {
		try {
			Class<?> listClass = Class.forName("java.util.List");
			if (propertyObtained.getClass().equals(listClass)) {
				return isArrayTypeMatching(propertyObtained, expectedPropertyType, listClass);
			}
		} catch (ClassNotFoundException e) { /* Will never happen */ }

		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean isArrayTypeMatching(Object propertyObtained, Object expectedPropertyType, Class<?> listClass) {
		List<Object> cast = (List<Object>) listClass.cast(propertyObtained);
		if (cast.size() > 0) {
			Object object = cast.get(0);
			if (object.getClass().equals(expectedPropertyType)) {
				return true;
			}
		}

		return false;
	}
	

}
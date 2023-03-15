package es.um.uschema.mongodb2uschema.spark.map;

import static es.um.uschema.mongodb2uschema.spark.constants.Constants.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.spark.api.java.function.PairFunction;
import org.bson.Document;

import es.um.uschema.mongodb2uschema.spark.utils.TypeUtils;
import scala.Tuple2;

public class ArchetypeMapping implements PairFunction<Document, TreeMap<String, Object>, Long> {

	private static final long serialVersionUID = 1L;
	
	private String collectionName;
	
	public ArchetypeMapping(String collectionName) {
		this.collectionName = collectionName;
	}

	@Override
	public Tuple2<TreeMap<String, Object>, Long> call(Document document) throws Exception {
		TreeMap<String,Object> schema = new TreeMap<>();
		schema.put(ENTITY, collectionName);
		
		TreeMap<String, Object> properties = new TreeMap<>();
		schema.put(PROPERTIES, properties);
		
		document.entrySet().forEach(e -> {
			String propertyName = e.getKey();
			Object propertyValue = e.getValue();
			
			processDataElement(properties, propertyName, propertyValue, false);
		});

		return new Tuple2<TreeMap<String,Object>, Long>(schema, 1L);
	}

	private void processDataElement(TreeMap<String, Object> properties, String propertyName, Object propertyValue, boolean isAnArray) {
		if (propertyValue instanceof Document) {			
			mapAggregateObject(properties, propertyName, (Document) propertyValue, isAnArray);
		} else if (propertyValue instanceof List) {		
			List<?> documentsArray = (List<?>) propertyValue;
			
			Set<Object> arrayList = new HashSet<>();
			properties.put(propertyName, arrayList);

			if (documentsArray.isEmpty()) {
				properties.put(propertyName, OPENING_SQUARE_BRACKETS + CLOSING_SQUARE_BRACKETS);
			} else if (!(documentsArray.get(0) instanceof Document)) {
				properties.put(propertyName, OPENING_SQUARE_BRACKETS + TypeUtils.obtainType(documentsArray.get(0)) + CLOSING_SQUARE_BRACKETS); 
			} else {
				for (Object doc : documentsArray) {
					if (doc instanceof Document) {
						TreeMap<String,Object> arrayPropertiesMap = new TreeMap<>();
						processDataElement(arrayPropertiesMap, propertyName, doc, true);
						arrayList.add(arrayPropertiesMap.get(propertyName));
					} 
				}
			}
		} else {
			properties.put(propertyName, isAnArray ? OPENING_SQUARE_BRACKETS + TypeUtils.obtainType(propertyValue) + CLOSING_SQUARE_BRACKETS : TypeUtils.obtainType(propertyValue));
		}
	}

	private void mapAggregateObject(TreeMap<String, Object> propertiesMap, String propertyName, Document jsonObject, boolean isAnArray) {
		TreeMap<String, Object> aggregateProperties = new TreeMap<>();
		propertiesMap.put(propertyName, aggregateProperties);
		
		jsonObject.entrySet().forEach(p -> {
			String aggregatePropertyName = p.getKey();
			aggregateProperties.put(aggregatePropertyName, TypeUtils.obtainType(p.getValue()));
		});
	}

}

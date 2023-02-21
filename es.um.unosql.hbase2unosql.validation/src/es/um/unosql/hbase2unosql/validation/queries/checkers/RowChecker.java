package es.um.unosql.hbase2unosql.validation.queries.checkers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.hbase.client.Result;

import es.um.unosql.hbase2unosql.validation.queries.utils.TypeUtils;
import es.um.unosql.hbase2unosql.validation.queries.utils.TypeUtils2;
import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class RowChecker
{
	private StructuralVariation mainStructuralVariation;
	@SuppressWarnings("unused")
	private HashMap<String, Class<?>> propertiesMap;

	public RowChecker(StructuralVariation structuralVariation) {
		this.mainStructuralVariation = structuralVariation;
		this.propertiesMap = createPropertiesMap();
	}
	
	private HashMap<String,Class<?>> createPropertiesMap() {
		final HashMap<String,Class<?>> propertiesMap = new HashMap<String, Class<?>>();
		
		mainStructuralVariation.getFeatures().stream()
			.filter(Attribute.class::isInstance).map(Attribute.class::cast).forEach(a -> {
				DataType type = a.getType();
				if (type instanceof PrimitiveType) {
					propertiesMap.put(a.getName(), TypeUtils2.getClassType((PrimitiveType) type));
				} else if (type instanceof PList) {
					processList(propertiesMap, a, (PList) type);
				} 
			});
		
		mainStructuralVariation.getFeatures().stream()
			.filter(Aggregate.class::isInstance).map(Aggregate.class::cast).forEach(a -> {
				processAggregate(propertiesMap, a);
			});
		
		return propertiesMap;
	}

	private void processAggregate(final HashMap<String, Class<?>> propertiesMap, Aggregate a) {
		try {
			propertiesMap.put(a.getName(), Class.forName("Result"));
		} catch (ClassNotFoundException e) { }
	}

	public List<Result> reduceVariationRows(List<Result> rowResults)
	{
		List<Result> results = new LinkedList<Result>();
		
		for (Result result : rowResults)
		{
			boolean present = true;
			
			NavigableMap<byte[], NavigableMap<byte[], byte[]>> familyColumnsMap = result.getNoVersionMap();
			for (Aggregate aggregate: mainStructuralVariation.getStructuralFeatures().stream().filter(Aggregate.class::isInstance).map(Aggregate.class::cast).collect(Collectors.toList()))
			{
				String familyColumn = aggregate.getName();
				NavigableMap<byte[], byte[]> columnsMap = familyColumnsMap.get(familyColumn.getBytes());
				Map<String, byte[]> map = new HashMap<>();
				columnsMap.entrySet().forEach(e -> {
					map.put(new String(e.getKey()), e.getValue());
				});
					
				for (StructuralVariation innerSV : aggregate.getAggregates())
				{
					for (Attribute innerSF: innerSV.getStructuralFeatures().stream().filter(Attribute.class::isInstance).map(Attribute.class::cast).collect(Collectors.toList()))
					{
						String initialColumnName = innerSF.getName();
						String columnName = innerSF.getName();
						byte[] columnValue = columnsMap.get(columnName.getBytes());
						
						if (columnValue == null) {
							Optional<String> findFirst = columnsMap.keySet().stream().map(String::new).filter(k -> k.endsWith(initialColumnName)).findFirst();
							if (!findFirst.isPresent())
								present = false;
							else {
								columnName = findFirst.get();
								columnValue = columnsMap.get(columnName.getBytes());
							}
						} 
						
						if (present) {
							present = arePropertiesMatching(present, innerSF, columnValue);
						}
					};
				}
			};
			

			if (present) 
				results.add(result);
		}
		
		return results;
	}

	private boolean arePropertiesMatching(boolean present, Attribute innerSF, byte[] columnValue) {
		Object deserialize = SerializationUtils.deserialize(columnValue);
		String valueAsString = (String) deserialize;
		String obtainSingleValueType = TypeUtils.obtainSingleValueType(valueAsString);
		
		boolean typeMatching = isTypeMatching(innerSF.getType(), obtainSingleValueType);
		if (!typeMatching) {
			present = false;
		}
		return present;
	}

	private boolean isTypeMatching(DataType type, String obtainSingleValueType) {
		if (type instanceof PrimitiveType)
		{
			PrimitiveType primitiveType = (PrimitiveType) type;
			
			return primitiveType.getName().equals(obtainSingleValueType);
		}
		
		return false;
	}

	private void processList(HashMap<String, Class<?>> propertiesMap, Attribute a, PList pList) {
		DataType dataType = pList.getElementType();
		if (dataType instanceof PrimitiveType) {
			propertiesMap.put(a.getName(), TypeUtils2.getClassType((PrimitiveType) dataType));
		}
	}

}
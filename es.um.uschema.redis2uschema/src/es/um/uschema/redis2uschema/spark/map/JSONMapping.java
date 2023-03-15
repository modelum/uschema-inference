package es.um.uschema.redis2uschema.spark.map;

import org.apache.spark.api.java.function.PairFunction;

import es.um.uschema.redis2uschema.utils.TypeUtils;

import static es.um.uschema.redis2uschema.constants.Constants.CLOSING_BRACKET;
import static es.um.uschema.redis2uschema.constants.Constants.CLOSING_SQUARE_BRACKETS;
import static es.um.uschema.redis2uschema.constants.Constants.COLON;
import static es.um.uschema.redis2uschema.constants.Constants.COMMA;
import static es.um.uschema.redis2uschema.constants.Constants.DOT;
import static es.um.uschema.redis2uschema.constants.Constants.DOUBLE_QUOTE_ESCAPED;
import static es.um.uschema.redis2uschema.constants.Constants.ENTITY;
import static es.um.uschema.redis2uschema.constants.Constants.OPENING_BRACKET;
import static es.um.uschema.redis2uschema.constants.Constants.OPENING_SQUARE_BRACKETS;
import static es.um.uschema.redis2uschema.constants.Constants.PROPERTIES;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import scala.Tuple2;

public class JSONMapping implements PairFunction<Tuple2<Collection<String>, Long>, String, Long>
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public Tuple2<String, Long> call(Tuple2<Collection<String>, Long> pair) throws Exception
	{
		Collection<String> set = pair._1;
		
		TreeMap<String,Object> jsonNode = new TreeMap<String, Object>();
		TreeMap<String,Object> properties = new TreeMap<String, Object>();
		jsonNode.put(PROPERTIES, properties);
		jsonNode.put("$_COUNT", pair._2);
		
		for (String keyValue : set) 
		{
			String entity = keyValue.substring(0, keyValue.indexOf(COLON));
			String property = keyValue.substring(keyValue.lastIndexOf(COLON) + 1);

			jsonNode.put(ENTITY, entity);
			
			if (property.contains("."))
			{
				int ldop = property.lastIndexOf(DOT);
				String subproperty = property.substring(ldop + 1);
				property = property.substring(0, ldop);
				if (property.contains(DOT))
					property = property.substring(0, property.lastIndexOf(DOT)) + "*";

				TreeMap<String,Object> subproperties = (TreeMap<String,Object>) properties.get(property);
				if (subproperties == null)
				{
					subproperties = new TreeMap<String, Object>();
					properties.put(property, subproperties);
				}
				
				subproperties.put(subproperty, TypeUtils.obtainSingleValueType("s"));
			} else {
				properties.put(property, TypeUtils.obtainSingleValueType("s"));
			}
		}
		
		return new Tuple2<String, Long>(generateJsonText(jsonNode), (long) jsonNode.get("$_COUNT"));
	}

	@SuppressWarnings("unchecked")
	private String generateJsonText(Map<String, Object> schema)
	{
		return OPENING_BRACKET + 
				schema.entrySet().stream()
					.map(e -> DOUBLE_QUOTE_ESCAPED + e.getKey() + DOUBLE_QUOTE_ESCAPED + ": " + 
							(e.getValue() instanceof Map<?, ?> ? generateJsonText((Map<String, Object>) e.getValue()) : 
								e.getValue() instanceof Set<?> ? generateJsonText((Set<TreeMap<String, Object>>) e.getValue()) : 
									e.getValue() instanceof String ? 
											((String) e.getValue()).startsWith(OPENING_SQUARE_BRACKETS) && ((String) e.getValue()).endsWith(CLOSING_SQUARE_BRACKETS) ? 
													e.getValue() : DOUBLE_QUOTE_ESCAPED + e.getValue() + DOUBLE_QUOTE_ESCAPED : e.getValue()))
				.collect(Collectors.joining(COMMA)) + 
			CLOSING_BRACKET;
	}
	
	private String generateJsonText(Set<TreeMap<String, Object>> schema)
	{
		return OPENING_SQUARE_BRACKETS + 
				schema.stream().map(this::generateJsonText).collect(Collectors.joining(COMMA)) + 
		CLOSING_SQUARE_BRACKETS;
	}
}

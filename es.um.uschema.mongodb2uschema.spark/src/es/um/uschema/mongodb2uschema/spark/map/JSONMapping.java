package es.um.uschema.mongodb2uschema.spark.map;

import org.apache.spark.api.java.function.PairFunction;

import static es.um.uschema.mongodb2uschema.spark.constants.Constants.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import scala.Tuple2;

public class JSONMapping implements PairFunction<Tuple2<TreeMap<String, Object>, Long>, String, Long>
{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<String, Long> call(Tuple2<TreeMap<String, Object>, Long> pair) throws Exception
	{
		TreeMap<String, Object> map = pair._1;
		
		return new Tuple2<String, Long>(generateJsonText(map), pair._2);
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

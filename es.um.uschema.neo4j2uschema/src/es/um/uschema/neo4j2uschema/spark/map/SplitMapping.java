package es.um.uschema.neo4j2uschema.spark.map;

import static es.um.uschema.neo4j2uschema.constants.Constants.CLOSING_BRACKET;
import static es.um.uschema.neo4j2uschema.constants.Constants.CLOSING_SQUARE_BRACKETS;
import static es.um.uschema.neo4j2uschema.constants.Constants.COMMA;
import static es.um.uschema.neo4j2uschema.constants.Constants.DOUBLE_QUOTE_ESCAPED;
import static es.um.uschema.neo4j2uschema.constants.Constants.OPENING_BRACKET;
import static es.um.uschema.neo4j2uschema.constants.Constants.OPENING_SQUARE_BRACKETS;
import static es.um.uschema.neo4j2uschema.constants.Constants.REFERENCES;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.spark.api.java.function.FlatMapFunction;

import scala.Tuple2;

public class SplitMapping implements FlatMapFunction<Tuple2<Long, TreeMap<String, Object>>, String>
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<String> call(Tuple2<Long, TreeMap<String, Object>> tuple) throws Exception
	{
		TreeMap<String, Object> schema = tuple._2;
		
		List<String> result = new ArrayList<String>();
		result.add(generateJsonText(schema));
		
		TreeSet<TreeMap<String, Object>> references = (TreeSet<TreeMap<String, Object>>) schema.get(REFERENCES);
		references.forEach(r -> result.add(generateJsonText(r)));
		
		return result.iterator();
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
	
	private String generateJsonText(Set<TreeMap<String, Object>> schema) {

		return OPENING_SQUARE_BRACKETS + 
				schema.stream().map(r -> generateJsonText(r)).collect(Collectors.joining(COMMA)) + 
		CLOSING_SQUARE_BRACKETS;
	}
}

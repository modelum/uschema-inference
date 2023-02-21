package es.um.unosql.neo4j.queries.utils;

import static es.um.unosql.neo4j.queries.constants.Constants.EMPTY;
import static es.um.unosql.neo4j.queries.constants.Constants.LABELS_JOINER;

import java.util.List;

public class StringNameUtils
{
	
	@SuppressWarnings("unchecked")
	public static String targetLabels(Object targetLabels)
	{
		String result = EMPTY;
		
		if (targetLabels instanceof List) 
		{
			result = String.join(LABELS_JOINER, (List<String>) targetLabels);
		}
		else if (targetLabels instanceof String)
		{
			result = (String) targetLabels;
		}
			
		return result;
	}

}

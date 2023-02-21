package es.um.unosql.neo4j2unosql.utils;

import static org.neo4j.driver.internal.types.InternalTypeSystem.TYPE_SYSTEM;

import static es.um.unosql.neo4j2unosql.constants.Constants.OPENING_SQUARE_BRACKETS;
import static es.um.unosql.neo4j2unosql.constants.Constants.CLOSING_SQUARE_BRACKETS;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.neo4j.driver.Value;

public class TypeUtils
{
	private static final String BOOLEAN = "boolean";
	private static final String DOUBLE  = "double";
	private static final String FLOAT  = "float";
	private static final String LONG = "long";
	private static final String INTEGER = "integer";
	private static final String STRING  = "string";

	private static final String ANY = "any";
	private static final String NULL = "null";
	private static final boolean SHORT_BOOLEAN = false;
	private static final double SHORT_DOUBLE  = 0.0;
	@SuppressWarnings("unused")
	private static final float SHORT_FLOAT  = 0.0f;
	@SuppressWarnings("unused")
	private static final long SHORT_INTEGER = 0;
	private static final long SHORT_LONG = 0L;
	private static final String SHORT_STRING  = "s";
	
	public static Object obtainType(Value value)
	{
		if (value.hasType(TYPE_SYSTEM.LIST()))
			return new Object[] {obtainType(value.values())};
		else if (value.hasType(TYPE_SYSTEM.BOOLEAN()))
			return SHORT_BOOLEAN;
		else if (value.hasType(TYPE_SYSTEM.STRING()))
			return SHORT_STRING;
		else if (value.hasType(TYPE_SYSTEM.FLOAT()))
			return SHORT_DOUBLE;
		else if (value.hasType(TYPE_SYSTEM.INTEGER()))
			return SHORT_LONG;

		return NULL;
	}
	
	
	private static Object obtainType(Iterable<Value> values)
	{
		List<Object> types = StreamSupport.stream(values.spliterator(), false)
			.map(TypeUtils::obtainType)
			.distinct()
			.collect(Collectors.toList());
					
		return types.size() == 1 ? types.get(0) : ANY;
	}


	public static String getTypeName(Object shortType)
	{
		if (shortType instanceof JSONArray) 
		{
			JSONArray array = (JSONArray) shortType;
			if (array.length() > 0)
				return geetSimpleType(array.get(0)) + OPENING_SQUARE_BRACKETS + CLOSING_SQUARE_BRACKETS;
		}
		
		return geetSimpleType(shortType);
	}


	private static String geetSimpleType(Object shortType)
	{
		if (shortType instanceof Boolean)
			return BOOLEAN;
		else if (shortType instanceof String)
			return STRING;
		else if (shortType instanceof Double)
			return DOUBLE;
		else if (shortType instanceof Float)
			return FLOAT;
		else if (shortType instanceof Long)
			return LONG;
		else if (shortType instanceof Integer)
			return INTEGER;
		else return ANY;
	}

}

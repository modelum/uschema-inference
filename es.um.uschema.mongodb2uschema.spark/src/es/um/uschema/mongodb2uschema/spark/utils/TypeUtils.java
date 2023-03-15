package es.um.uschema.mongodb2uschema.spark.utils;

import static es.um.uschema.mongodb2uschema.spark.constants.Constants.*;

import java.util.List;

import org.json.JSONArray;

public class TypeUtils
{
	private static final String BOOLEAN = "Boolean";
	private static final String DOUBLE  = "Double";
	@SuppressWarnings("unused")
	private static final String FLOAT  = "Double";
	private static final String LONG = "Number";
	@SuppressWarnings("unused")
	private static final String INTEGER = "Number";
	private static final String STRING  = "String";

	@SuppressWarnings("unused")
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
	
	public static Object obtainType(Object value)
	{
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			return new Object[] {obtainType(list.get(0))};
		} else if (value.getClass().isArray()) {
			Object[] array = (Object[]) value;
			return new Object[] {obtainType(array[0])};
		} else if (value instanceof Boolean)
			return SHORT_BOOLEAN;
		else if (value instanceof String)
			return SHORT_STRING;
		else if (value instanceof Double || value instanceof Float)
			return SHORT_DOUBLE;
		else if (value instanceof Long || value instanceof Integer)
			return SHORT_LONG;

		return NULL;
	}

	public static String getTypeAsString(Object shortType)
	{
		if (shortType instanceof List) {
			List<?> list = (List<?>) shortType;
			return LIST + OPENING_SQUARE_BRACKETS + getTypeAsString(list.get(0)) + CLOSING_SQUARE_BRACKETS;
		} else if (shortType.getClass().isArray()) {
			Object[] array = (Object[]) shortType;
			return ARRAY + OPENING_SQUARE_BRACKETS + getTypeAsString(array[0]) + CLOSING_SQUARE_BRACKETS;
		} else if (shortType instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) shortType;
			return ARRAY + OPENING_SQUARE_BRACKETS + getTypeAsString(jsonArray.get(0)) + CLOSING_SQUARE_BRACKETS;
		} else if (shortType instanceof Boolean)
			return BOOLEAN;
		else if (shortType instanceof String)
			return STRING;
		else if (shortType instanceof Double || shortType instanceof Float)
			return DOUBLE;
		else if (shortType instanceof Long || shortType instanceof Integer)
			return LONG;
		
		return STRING;
	}

}

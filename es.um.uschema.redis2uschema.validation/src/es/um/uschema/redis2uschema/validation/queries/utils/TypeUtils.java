package es.um.uschema.redis2uschema.validation.queries.utils;

import java.util.Collection;

public class TypeUtils {
	public static final String BOOLEAN = "boolean";
	public static final String DOUBLE = "double";
	public static final String INTEGER = "integer";
	public static final String STRING = "string";

	public static final String FALSE = "false";
	public static final String TRUE = "true";

	private static final String EMPTY = "";

	public static String obtainSingleValueType(String value) {
		if (value.isEmpty())
			return EMPTY;
		else if (value.toLowerCase().equals(TRUE) || value.toLowerCase().equals(FALSE))
			return BOOLEAN;
		else if (value.matches("[0-9]+\\.[0-9]+"))
			return DOUBLE;
		else if (value.matches("[0-9]+"))
			return INTEGER;
		else
			return STRING;
	}

	public static String getCollectionType(Collection<String> listK) {
		String commonElementType = STRING;
		for (String listElement : listK) {
			String elementType = obtainSingleValueType(listElement);
			if (!commonElementType.equals(elementType)) {
				if (commonElementType.equals(INTEGER) && elementType.equals(DOUBLE))
					commonElementType = DOUBLE;
				else
					commonElementType = STRING;
			}
		}

		return commonElementType;
	}

}

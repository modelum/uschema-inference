package es.um.unosql.neo4j.queries.utils;

import static es.um.unosql.neo4j.queries.constants.Constants.*;

import es.um.unosql.uNoSQLSchema.PrimitiveType;

public class TypeUtils {
	
	public static Class<?> getClassType(PrimitiveType type) {
		String className = JAVA_LANG_PACKAGE;

		switch (type.getName()) {
			case STRING:
				className += STRING_FIRST_UPPERCASE;
				break;
			case LONG:
				className += LONG_FIRST_UPPERCASE;
				break;
			case DOUBLE:
				className += DOUBLE_FIRST_UPPERCASE;
				break;
			case BOOLEAN:
				className += BOOLEAN_FIRST_UPPERCASE;
				break;
		}

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null; // Will never happens
		}
	}

}

package es.um.unosql.hbase2unosql.validation.queries.utils;

import static es.um.unosql.hbase2unosql.validation.queries.constants.Constants.*;

import es.um.unosql.uNoSQLSchema.PrimitiveType;

public class TypeUtils2 {
	public static Class<?> getClassType(PrimitiveType type) {
		String className = JAVA_LANG_PACKAGE;

		switch (type.getName()) {
			case STRING_FIRST_UPPERCASE: 	className += STRING_FIRST_UPPERCASE;  break;
			case LONG_FIRST_UPPERCASE: 		className += LONG_FIRST_UPPERCASE;    break;
			case DOUBLE_FIRST_UPPERCASE: 	className += DOUBLE_FIRST_UPPERCASE;  break;
			case BOOLEAN_FIRST_UPPERCASE:	className += BOOLEAN_FIRST_UPPERCASE; break;
		}

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) { 
			return null; // Will never happens
		}
	}

}

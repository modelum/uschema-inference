package es.um.unosql.doc2unosql.validation.queries.utils;

import static es.um.unosql.doc2unosql.validation.queries.constants.Constants.*;

import es.um.unosql.uNoSQLSchema.PrimitiveType;

public class TypeUtils {
	public static Class<?> getClassType(PrimitiveType type) {
		String className = JAVA_LANG_PACKAGE;

		switch (type.getName()) {
			case STRING_FIRST_UPPERCASE: 	className += STRING_FIRST_UPPERCASE;  break;
			case LONG_FIRST_UPPERCASE: 		className += LONG_FIRST_UPPERCASE;    break;
			case DOUBLE_FIRST_UPPERCASE: 	className += DOUBLE_FIRST_UPPERCASE;  break;
			case BOOLEAN_FIRST_UPPERCASE:	className += BOOLEAN_FIRST_UPPERCASE; break;
			case NUMBER_FIRST_UPPERCASE:	className += INTEGER_FIRST_UPPERCASE; break;
			case INTEGER_FIRST_UPPERCASE:	className += INTEGER_FIRST_UPPERCASE; break;
		}

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) { 
			return null; // Never happens
		}
	}

}

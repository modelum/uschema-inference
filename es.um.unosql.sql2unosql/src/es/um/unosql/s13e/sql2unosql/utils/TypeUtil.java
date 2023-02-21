package es.um.unosql.s13e.sql2unosql.utils;

public class TypeUtil {

	public static String convert(String type) {
		switch (type) {
			case "CHAR": 			return "String";
			case "VARCHAR": 		return "String";
			case "LONGVARCHAR": 	return "String";
			case "NUMERIC": 		return "long double";
			case "DECIMAL": 		return "long double";
			case "BIT": 			return "boolean";
			case "TINYINT": 		return "byte";
			case "SMALLINT": 		return "short";
			case "INTEGER": 		return "integer";
			case "BIGINT": 			return "long";
			case "REAL": 			return "float";
			case "FLOAT": 			return "double";
			case "DOUBLE": 			return "double";
			case "BINARY": 			return "byte[]";
			case "VARBINARY": 		return "byte[]";
			case "LONGVARBINARY": 	return "byte[]";
			case "DATE": 			return "date";
			case "TIME": 			return "time";
			case "TIMESTAMP": 		return "timestamp";
			default:				return "unknown";
		}
	}
	
	
}

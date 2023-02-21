package es.um.unosql.neo4j2unosql.constants;

import java.util.regex.Pattern;

public class Constants
{
	
	/* SchemaMapping */
	public static final String LABELS = "labels";
	public static final String TYPE = "type";
	public static final String REFS_TO = "refsTo";
	public static final String ENTITY = "entity";
	public static final String REFERENCES = "references";
	public static final String PROPERTIES = "properties";
	
	public static final String NODE = "node";
	public static final String RELATIONSHIP = "relationship";
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	public static final String DOUBLE_QUOTE_ESCAPED = "\"";
	public static final String TYPE_PATTERN = "\"type\":\"";
	public static final String SLASH = "/";
	public static final String COMMA = ",";
	public static final String DOT = ".";
	public static final String COLON = ":";
	public static final String SQUARE_BRACKET_COMA_SEPARATOR = ",\"";
	public static final String COMMA_SEPARATOR = ",\"";
	public static final String GRAVE_ACCENT = "`";
	

	public static final Pattern NODE_WITH_RELATIONSHOPS_PATTERN = Pattern.compile("}\"[,]\"\\{");
	public static final Pattern NODE_ONLY_PATTERN = Pattern.compile("\",\"\",\"\"");
	
	public static final String LABELS_PATTERN = "\"labels\":[";
	public static final String LABELS_PATTERN_END_PATTERN = "]";
	
	public static final String PROPERTIES_PATTERN = "\"properties\":{";
	public static final String PROPERTIES_END_PATTERN = "}";
	
	/* Common */
	public static final String EMPTY = "";
	public static final String LABELS_JOINER = "_AND_";    // If changed Here, Change other project Constants: es.um.nosql.s13e.neo4j.queries
	public static final String SPECIAL_CHARS = "$_";

	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";

	public static final String OPENING_SQUARE_BRACKETS = "[";
	public static final String CLOSING_SQUARE_BRACKETS = "]";
	
	public static final String ARRAY = "[]";
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final String _ID = "_id";
	public static final String STRING = "String";
	
}

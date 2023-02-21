package es.um.unosql.neo4j.queries.constants;

public class Constants
{

	// String
	public static final String EMPTY = "";
	public static final String SPACE = " ";
	public static final String TAB = "\t";
	public static final String LINE = System.getProperty("line.separator");

	// Types
	public static final String STRING  = "string";
	public static final String STRING_FIRST_UPPERCASE  = "String";
	public static final String LONG = "long";
	public static final String LONG_FIRST_UPPERCASE = "Long";
	public static final String LONG_SHORT = "l";
	public static final String DOUBLE  = "double";
	public static final String DOUBLE_FIRST_UPPERCASE  = "Double";
	public static final String DOUBLE_SHORT  = "d";
	public static final String BOOLEAN = "boolean";
	public static final String BOOLEAN_FIRST_UPPERCASE = "Boolean";
	public static final String BOOLEAN_SHORT = "b";
	
	// Neo4j Node Row 
	public static final int NODE_COLUMNS_NUMBER = 8;
	public static final int NODE_LABELS = 7;
	public static final int NODE_REL_TARGET_NODE_ID = 6;
	public static final int NODE_REL_TARGET_NODE_TYPE = 5;
	public static final int NODE_REL_TYPE = 4;
	public static final int NODE_REL_IDS = 3;
	public static final int NODE_REL_PROPERTIES = 2;
	public static final int NODE_PROPERTIES = 1;
	public static final int NODE_ID = 0;

	// Neo4j Reference Row 
	public static final int REL_COLUMNS_NUMBER = 6;
	public static final int REL_TARGET_LABELS = 5;
	public static final int REL_ORIGIN_LABELS = 4;
	public static final int REL_ORIGIN_NODE_ID = 3;
	public static final int REL_TARGET_NODE_ID = 2;
	public static final int REL_PROPERTIES = 1;
	public static final int REL_ID = 0;

	public static final String _ID = "_id";
	
	public static final String JAVA_LANG_PACKAGE = "java.lang.";

	public static final String LABELS_JOINER = "_AND_";    // If changed Here, Change other project Constants: es.um.nosql.s13e.neo4j2dbschema.spark
	public static final String BACKTICK = "`";

	public static final String OPENING_SQUARE_BRACKET = "[";
	
	public static final String LIST_TYPE = "java.util.Collections$UnmodifiableRandomAccessList";
}

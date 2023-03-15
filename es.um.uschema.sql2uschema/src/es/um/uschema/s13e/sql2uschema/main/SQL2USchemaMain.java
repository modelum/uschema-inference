package es.um.uschema.s13e.sql2uschema.main;

import es.um.uschema.s13e.sql2uschema.SQL2USchema;

public class SQL2USchemaMain
{
	public static void main(String[] args)
	{
		SQL2USchema sql2uschema = new SQL2USchema();
		sql2uschema.createSchema("jdbc:mysql://localhost/sakila", "root", "root");
		sql2uschema.toXMI("./outputs/sql-uschema.xmi");
	}
}

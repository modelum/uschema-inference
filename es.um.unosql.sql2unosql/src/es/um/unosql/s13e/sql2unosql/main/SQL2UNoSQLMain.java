package es.um.unosql.s13e.sql2unosql.main;

import es.um.unosql.s13e.sql2unosql.SQL2UNoSQL;

public class SQL2UNoSQLMain
{
	public static void main(String[] args)
	{
		SQL2UNoSQL sql2unosql = new SQL2UNoSQL();
		sql2unosql.createSchema("jdbc:mysql://localhost/sakila", "root", "root");
		sql2unosql.toXMI("./outputs/sql-unosql.xmi");
	}
}

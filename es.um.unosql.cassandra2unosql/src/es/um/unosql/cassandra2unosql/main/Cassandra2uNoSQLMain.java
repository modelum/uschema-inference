package es.um.unosql.cassandra2unosql.main;

import es.um.unosql.cassandra2unosql.Cassandra2uNoSQL;

public class Cassandra2uNoSQLMain {

	public static void main(String args[]) {
		System.setProperty("hadoop.home.dir", "./hadoop");
		
		Cassandra2uNoSQL cassandra2unosql = new Cassandra2uNoSQL();
		cassandra2unosql.cassandra2unosql("Cycling","cycling", "127.0.0.1", 9042);
		cassandra2unosql.toXMI("./outputs/cassandra-unosql.xmi");
	}
	
}
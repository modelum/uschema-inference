package es.um.uschema.cassandra2uschema.main;

import es.um.uschema.cassandra2uschema.Cassandra2USchema;

public class Cassandra2USchemaMain {

	public static void main(String args[]) {
		System.setProperty("hadoop.home.dir", "./hadoop");
		
		Cassandra2USchema cassandra2uschema = new Cassandra2USchema();
		cassandra2uschema.cassandra2uschema("Cycling","cycling", "127.0.0.1", 9042);
		cassandra2uschema.toXMI("./outputs/cassandra-uschema.xmi");
	}
	
}
package es.um.unosql.cassandra2unosql;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.spark.connector.japi.rdd.CassandraTableScanJavaRDD;

import es.um.unosql.cassandra2unosql.spark.map.CassandraRowToColumnListMapping;
import es.um.unosql.cassandra2unosql.utils.TypeMappingUtil;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Key;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLFactory;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class Cassandra2uNoSQL
{
	private static final String SPARK_CASSANDRA_CONNECTION_HOST = "spark.cassandra.connection.host";
//	private static final String SPARK_CASSANDRA_CONNECTION_NATIVE_PORT = "spark.cassandra.connection.native.port";
//	private static final String SPARK_CASSANDRA_AUTH_USERNAME = "spark.cassandra.auth.username";
//	private static final String SPARK_CASSANDRA_AUTH_PASSWORD = "spark.cassandra.auth.password";

	private static final String APP_NAME = "Cassandra";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";

	private UNoSQLFactory uNoSQLFactory;
	private uNoSQLSchema uNoSQLSchema;

	public Cassandra2uNoSQL() {
		this.uNoSQLFactory = new UNoSQLFactory();
	}

	public void cassandra2unosql(String dataCenter, String databaseName, String databaseUri, int databasePort)
	{
		SparkConf conf = new SparkConf()
				.setAppName(APP_NAME)
				.setMaster(MASTER)
				.set(SPARK_CASSANDRA_CONNECTION_HOST, databaseUri);
//				.set(SPARK_CASSANDRA_CONNECTION_NATIVE_PORT, Integer.toString(databasePort));
//		        .set(SPARK_CASSANDRA_AUTH_USERNAME, "cassandra")
//		        .set(SPARK_CASSANDRA_AUTH_PASSWORD, "cassandra");
		SparkContext sc = new SparkContext(conf);
		process(dataCenter, databaseName, databaseUri, databasePort, sc);
		sc.stop();
	}

	private void process(String dataCenter, String databaseName, String databaseUri, int databasePort,
			SparkContext sc)
	{
		try (CqlSession session = CqlSession.builder()
	            .withLocalDatacenter(dataCenter)
				.addContactPoint(new InetSocketAddress(databaseUri, databasePort)).build())
		{
			uNoSQLSchema = uNoSQLFactory.createUNoSQLSchema(databaseName);

			processKeySpaces(databaseName, sc, session, uNoSQLSchema);

			UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
			uNoSQLSchemaWriter.write(uNoSQLSchema, new File("./outputs/model.xmi"));
		} catch (com.datastax.oss.driver.api.core.AllNodesFailedException e) {
			e.printStackTrace();
		}
	}

	private void processKeySpaces(String databaseName, SparkContext sc, CqlSession session, uNoSQLSchema uNoSQLSchema)
	{
		Map<CqlIdentifier, KeyspaceMetadata> keyspaces = session.getMetadata().getKeyspaces();
		for (CqlIdentifier keyspace : keyspaces.keySet())
		{
			if (keyspace.toString().equals(databaseName))
			{
				KeyspaceMetadata keyspaceMetadata = keyspaces.get(keyspace);
				for (TableMetadata table : keyspaceMetadata.getTables().values())
					processTable(sc, uNoSQLSchema, keyspace, table);
			}
		}
	}

	private void processTable(SparkContext sc, uNoSQLSchema uNoSQLSchema, CqlIdentifier keyspace, TableMetadata table) {
		EntityType entityType = uNoSQLFactory.createEntityType(table.getName().toString());
		uNoSQLSchema.getEntities().add(entityType);

		CassandraTableScanJavaRDD<CassandraRow> cassandraTable = CassandraJavaUtil.javaFunctions(sc)
		        .cassandraTable(keyspace.toString(), table.getName().toString());

		JavaRDD<SortedSet<String>> map = cassandraTable.map(new CassandraRowToColumnListMapping()).distinct();
		List<SortedSet<String>> columnsLists = map.collect();

		createVariations(table, entityType, columnsLists);
	}

	private void createVariations(TableMetadata table, EntityType entityType, List<SortedSet<String>> columnsLists) {
		int id = 0;
		for (SortedSet<String> columns : columnsLists)
		{
			StructuralVariation variation = uNoSQLFactory.createStructuralVariation(++id);
			entityType.getVariations().add(variation);

			Key key = uNoSQLFactory.createKey("PartitionKey");
			variation.getFeatures().add(key);
			variation.getLogicalFeatures().add(key);

			for (String columnName : columns) {
				for (ColumnMetadata columnMetadata : table.getPartitionKey()) {
					if (columnName.equals(columnMetadata.getName().toString()))
					{
						Attribute attribute = createAttribute(variation, columnName, columnMetadata);
						key.getAttributes().add(attribute);
					}
				}
				for (ColumnMetadata columnMetadata : table.getColumns().values()) {
					if (table.getPartitionKey().stream().filter(p -> p.getName().toString().equals(columnName)).count() == 0
						&& columnName.equals(columnMetadata.getName().toString()))
						createAttribute(variation, columnName, columnMetadata);
				}
			}
		}
	}

	private Attribute createAttribute(StructuralVariation variation, String columnName,
										ColumnMetadata columnMetadata)
	{
		com.datastax.oss.driver.api.core.type.DataType type = columnMetadata.getType();

		DataType modelDataType = TypeMappingUtil.getType(type, uNoSQLFactory);
		if (modelDataType != null)
		{
			Attribute attribute = uNoSQLFactory.createAttribute(columnName, modelDataType);
			variation.getFeatures().add(attribute);
			variation.getStructuralFeatures().add(attribute);

			return attribute;
		}

		return null;
	}

	public void toXMI(String outputRoute)
	{
		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputRoute));
	}

}
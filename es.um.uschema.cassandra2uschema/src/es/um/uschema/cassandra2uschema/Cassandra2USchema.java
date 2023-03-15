package es.um.uschema.cassandra2uschema;

import java.net.InetSocketAddress;
import java.nio.file.Path;
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

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Key;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.utils.USchemaFactory;
import es.um.uschema.cassandra2uschema.spark.map.CassandraRowToColumnListMapping;
import es.um.uschema.cassandra2uschema.utils.TypeMappingUtil;

public class Cassandra2USchema
{
	private static final String SPARK_CASSANDRA_CONNECTION_HOST = "spark.cassandra.connection.host";
//	private static final String SPARK_CASSANDRA_CONNECTION_NATIVE_PORT = "spark.cassandra.connection.native.port";
//	private static final String SPARK_CASSANDRA_AUTH_USERNAME = "spark.cassandra.auth.username";
//	private static final String SPARK_CASSANDRA_AUTH_PASSWORD = "spark.cassandra.auth.password";

	private static final String APP_NAME = "Cassandra";
	private static final String MASTER = "local[" + Runtime.getRuntime().availableProcessors() + "]";

	private USchemaFactory uSchemaFactory;
	private USchema uSchema;

	public Cassandra2USchema() {
		this.uSchemaFactory = new USchemaFactory();
	}

	public void cassandra2uschema(String dataCenter, String databaseName, String databaseUri, int databasePort)
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
		  uSchema = uSchemaFactory.createUSchema(databaseName);

			processKeySpaces(databaseName, sc, session, uSchema);

			EcoreModelIO uSchemaWriter = new EcoreModelIO();
			uSchemaWriter.write(uSchema, Path.of("./outputs/model.xmi"));
		} catch (com.datastax.oss.driver.api.core.AllNodesFailedException e) {
			e.printStackTrace();
		}
	}

	private void processKeySpaces(String databaseName, SparkContext sc, CqlSession session, USchema uSchema)
	{
		Map<CqlIdentifier, KeyspaceMetadata> keyspaces = session.getMetadata().getKeyspaces();
		for (CqlIdentifier keyspace : keyspaces.keySet())
		{
			if (keyspace.toString().equals(databaseName))
			{
				KeyspaceMetadata keyspaceMetadata = keyspaces.get(keyspace);
				for (TableMetadata table : keyspaceMetadata.getTables().values())
					processTable(sc, uSchema, keyspace, table);
			}
		}
	}

	private void processTable(SparkContext sc, USchema uSchema, CqlIdentifier keyspace, TableMetadata table) {
		EntityType entityType = uSchemaFactory.createEntityType(table.getName().toString());
		uSchema.getEntities().add(entityType);

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
			StructuralVariation variation = uSchemaFactory.createStructuralVariation(++id);
			entityType.getVariations().add(variation);

			Key key = uSchemaFactory.createKey("PartitionKey");
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

		DataType modelDataType = TypeMappingUtil.getType(type, uSchemaFactory);
		if (modelDataType != null)
		{
			Attribute attribute = uSchemaFactory.createAttribute(columnName, modelDataType);
			variation.getFeatures().add(attribute);
			variation.getStructuralFeatures().add(attribute);

			return attribute;
		}

		return null;
	}

	public void toXMI(String outputRoute)
	{
	  EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(uSchema, Path.of(outputRoute));
	}

}
package es.um.unosql.s13e.sql2unosql;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.um.unosql.s13e.sql2unosql.utils.TypeUtil;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Key;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLFactory;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class SQL2UNoSQL
{

	private static final int TABLE_NAME_INDEX = 3;

	private UNoSQLFactory uNoSQLFactory;

	private uNoSQLSchema uNoSQLSchema;

	public SQL2UNoSQL()
	{
		this.uNoSQLFactory = new UNoSQLFactory();
	}

	public void createSchema(String databaseUrl, String user, String password)
	{
		try {
			processTables(databaseUrl, user, password);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
		}
	}

	private void processTables(String databaseUrl, String user, String password) throws SQLException
	{
		Connection conn = DriverManager.getConnection(databaseUrl, user, password);
		String[] types = { "TABLE" };

		String dbName = databaseUrl.substring(databaseUrl.lastIndexOf('/'), databaseUrl.length());
		uNoSQLSchema = uNoSQLFactory.createLinkedUNoSQLSchema(dbName);

		ResultSet tablesResultSet = conn.getMetaData().getTables(null, null, "%", types);
		while (tablesResultSet.next())
		{
			createColumnsForTable(conn, uNoSQLSchema, tablesResultSet);
		}
		tablesResultSet.close();

		createKeys(conn, uNoSQLSchema);
		
		conn.close();
	}

	private void createColumnsForTable(Connection conn, uNoSQLSchema uNoSQLSchema, ResultSet tablesResultSet) throws SQLException
	{
		String tableName = tablesResultSet.getString(TABLE_NAME_INDEX);

		if (!tableName.startsWith("sys_")) {
			EntityType entityType = uNoSQLFactory.createEntityType(tableName);
			entityType.setRoot(true);
			uNoSQLSchema.getEntities().add(entityType);

			StructuralVariation structuralVariation = uNoSQLFactory.createStructuralVariation(entityType.getVariations().size() + 1);
			entityType.getVariations().add(structuralVariation);

			DatabaseMetaData meta = conn.getMetaData();
			columns(tableName, meta, structuralVariation);
		}
	}

	private void columns(String tableName, DatabaseMetaData meta, StructuralVariation structuralVariation) throws SQLException
	{
		ResultSet columnsResultSet = meta.getColumns(null, null, tableName, "%");
		while (columnsResultSet.next())
		{
			String columnName = columnsResultSet.getString("COLUMN_NAME");
			String columnType = columnsResultSet.getString("TYPE_NAME");

			DataType type = uNoSQLFactory.createPrimitiveType(TypeUtil.convert(columnType));
			Attribute attribute = uNoSQLFactory.createAttribute(columnName, type);
			structuralVariation.getFeatures().add(attribute);
			structuralVariation.getStructuralFeatures().add(attribute);
		}
		columnsResultSet.close();
	}

	private void createKeys(Connection conn, uNoSQLSchema uNoSQLSchema) throws SQLException
	{
		for (EntityType entityType : uNoSQLSchema.getEntities())
		{
			String tableName = entityType.getName();

			if (entityType.getVariations().size() > 0)
			{
				StructuralVariation structuralVariation = entityType.getVariations().get(0);

				DatabaseMetaData meta = conn.getMetaData();
				primaryKeys(tableName, meta, structuralVariation);
				foreignKeys(tableName, meta, uNoSQLSchema, structuralVariation);
			}
		}
	}

	private void primaryKeys(String tableName, DatabaseMetaData meta, StructuralVariation structuralVariation) throws SQLException
	{
		ResultSet primaryKeysResultSet = meta.getPrimaryKeys(null, null, tableName);
		while (primaryKeysResultSet.next())
		{
			String pkName = primaryKeysResultSet.getString("PK_NAME");
			String columnName = primaryKeysResultSet.getString("COLUMN_NAME");

			Key primaryKey = getPrimaryKey(structuralVariation, pkName);
			Attribute attribute = getAttribute(structuralVariation, columnName);
			if (attribute != null)
				primaryKey.getAttributes().add(attribute);
		}
	}

	private Attribute getAttribute(StructuralVariation structuralVariation, String columnName)
	{
		Optional<Attribute> possibleAttribute = structuralVariation.getStructuralFeatures()
				.stream()
					.filter(Attribute.class::isInstance).map(Attribute.class::cast)
					.filter(a -> a.getName().equals(columnName))
					.findFirst();

		return possibleAttribute.orElse(null);
	}

	private Key getPrimaryKey(StructuralVariation structuralVariation, String pkName)
	{
		Optional<Key> possibleKey = structuralVariation.getLogicalFeatures()
		.stream()
			.filter(Key.class::isInstance).map(Key.class::cast)
			.filter(k -> k.getName().equals(pkName)).findFirst();

		Key key = possibleKey.orElseGet(
				() -> {
					Key k = uNoSQLFactory.createKey(pkName);
					structuralVariation.getFeatures().add(k);
					structuralVariation.getLogicalFeatures().add(k);
					return k;
				});

		return key;
	}

	private void foreignKeys(String tableName, DatabaseMetaData meta, uNoSQLSchema uNoSQLSchema,
							StructuralVariation structuralVariation) throws SQLException
	{
		ResultSet foreignKeysResultSet = meta.getImportedKeys(null, null, tableName);
		while (foreignKeysResultSet.next())
		{
			String fkName = foreignKeysResultSet.getString("FK_NAME");
			String fkColumnName = foreignKeysResultSet.getString("FKCOLUMN_NAME");
			String targetTableName = foreignKeysResultSet.getString("PKTABLE_NAME");
//			String targetColumnName = foreignKeysResultSet.getString("PKCOLUMN_NAME");

			Reference reference = getReference(structuralVariation, fkName);
			Attribute attribute = getAttribute(structuralVariation, fkColumnName);
			if (attribute != null) reference.getAttributes().add(attribute);

			Optional<EntityType> possibleRefstoEntityType = uNoSQLSchema.getEntities().stream()
									.filter(e -> e.getName().equals(targetTableName)).findFirst();
			possibleRefstoEntityType.ifPresent(rt -> reference.setRefsTo(rt));
		}
		foreignKeysResultSet.close();
	}

	private Reference getReference(StructuralVariation structuralVariation, String fkName)
	{
		Optional<Reference> possibleReference = structuralVariation.getLogicalFeatures()
				.stream()
					.filter(Reference.class::isInstance).map(Reference.class::cast)
					.filter(k -> k.getName().equals(fkName))
					.findFirst();

		Reference reference = possibleReference.orElseGet(() -> {
				Reference ref = uNoSQLFactory.createReference(fkName);
				structuralVariation.getFeatures().add(ref);
				structuralVariation.getLogicalFeatures().add(ref);
				return ref;
			});

		return reference;
	}

	public void toXMI(String outputRoute)
	{
		UNoSQLSchemaWriter uNoSQLSchemaWriter = new UNoSQLSchemaWriter();
		uNoSQLSchemaWriter.write(uNoSQLSchema, new File(outputRoute));
	}
}

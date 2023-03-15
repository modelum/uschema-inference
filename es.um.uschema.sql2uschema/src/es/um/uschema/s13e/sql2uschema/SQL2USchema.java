package es.um.uschema.s13e.sql2uschema;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Key;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.s13e.sql2uschema.utils.TypeUtil;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.utils.USchemaFactory;

public class SQL2USchema
{

	private static final int TABLE_NAME_INDEX = 3;

	private USchemaFactory uFactory;

	private USchema uSchema;

	public SQL2USchema()
	{
		this.uFactory = new USchemaFactory();
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
		uSchema = uFactory.createLinkedUSchema(dbName);

		ResultSet tablesResultSet = conn.getMetaData().getTables(null, null, "%", types);
		while (tablesResultSet.next())
		{
			createColumnsForTable(conn, uSchema, tablesResultSet);
		}
		tablesResultSet.close();

		createKeys(conn, uSchema);
		
		conn.close();
	}

	private void createColumnsForTable(Connection conn, USchema uSchema, ResultSet tablesResultSet) throws SQLException
	{
		String tableName = tablesResultSet.getString(TABLE_NAME_INDEX);

		if (!tableName.startsWith("sys_")) {
			EntityType entityType = uFactory.createEntityType(tableName);
			entityType.setRoot(true);
			uSchema.getEntities().add(entityType);

			StructuralVariation structuralVariation = uFactory.createStructuralVariation(entityType.getVariations().size() + 1);
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

			DataType type = uFactory.createPrimitiveType(TypeUtil.convert(columnType));
			Attribute attribute = uFactory.createAttribute(columnName, type);
			structuralVariation.getFeatures().add(attribute);
			structuralVariation.getStructuralFeatures().add(attribute);
		}
		columnsResultSet.close();
	}

	private void createKeys(Connection conn, USchema uSchema) throws SQLException
	{
		for (EntityType entityType : uSchema.getEntities())
		{
			String tableName = entityType.getName();

			if (entityType.getVariations().size() > 0)
			{
				StructuralVariation structuralVariation = entityType.getVariations().get(0);

				DatabaseMetaData meta = conn.getMetaData();
				primaryKeys(tableName, meta, structuralVariation);
				foreignKeys(tableName, meta, uSchema, structuralVariation);
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
					Key k = uFactory.createKey(pkName);
					structuralVariation.getFeatures().add(k);
					structuralVariation.getLogicalFeatures().add(k);
					return k;
				});

		return key;
	}

	private void foreignKeys(String tableName, DatabaseMetaData meta, USchema uSchema,
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

			Optional<EntityType> possibleRefstoEntityType = uSchema.getEntities().stream()
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
				Reference ref = uFactory.createReference(fkName);
				structuralVariation.getFeatures().add(ref);
				structuralVariation.getLogicalFeatures().add(ref);
				return ref;
			});

		return reference;
	}

	public void toXMI(String outputRoute)
	{
	  EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(uSchema, Path.of(outputRoute));
	}
}

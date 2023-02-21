package es.um.unosql.cassandra2unosql.utils;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.internal.core.type.DefaultSetType;

import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.utils.UNoSQLFactory;

public class TypeMappingUtil {

	public static DataType getType(com.datastax.oss.driver.api.core.type.DataType type, UNoSQLFactory uNoSQLFactory) {
		DataType modelDataType = null;

		if (type instanceof com.datastax.oss.driver.internal.core.type.PrimitiveType) {
			modelDataType = createPrimitiveType(type, modelDataType, uNoSQLFactory);
		} else if (type instanceof com.datastax.oss.driver.internal.core.type.DefaultListType) {
			com.datastax.oss.driver.internal.core.type.DefaultListType defaultListType = (com.datastax.oss.driver.internal.core.type.DefaultListType) type;
			DataType elementType = getType(defaultListType.getElementType(), uNoSQLFactory);
			modelDataType = uNoSQLFactory.createPList(elementType);

		} else if (type instanceof com.datastax.oss.driver.internal.core.type.DefaultMapType) {
			com.datastax.oss.driver.internal.core.type.DefaultMapType defaultMapType = (com.datastax.oss.driver.internal.core.type.DefaultMapType) type;
			DataType keyType = getType(defaultMapType.getKeyType(), uNoSQLFactory);
			DataType valueType = getType(defaultMapType.getValueType(), uNoSQLFactory);

			if (keyType instanceof PrimitiveType)
				modelDataType = uNoSQLFactory.createPMap((PrimitiveType) keyType, valueType);
		} else if (type instanceof DefaultSetType) {
			DefaultSetType defaultSetType = (DefaultSetType) type;
			DataType elementType = getType(defaultSetType.getElementType(), uNoSQLFactory);
			modelDataType = uNoSQLFactory.createPSet(elementType);
		}

		return modelDataType;
	}

	private static DataType createPrimitiveType(com.datastax.oss.driver.api.core.type.DataType type, DataType modelDataType, UNoSQLFactory uNoSQLFactory) {
		if (DataTypes.TEXT.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("string");
		} else if (DataTypes.INT.equals(type) || DataTypes.SMALLINT.equals(type) || DataTypes.TINYINT.equals(type) || DataTypes.VARINT.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("integer");
		} else if (DataTypes.BIGINT.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("long");
		} else if (DataTypes.BOOLEAN.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("boolean");
		} else if (DataTypes.DOUBLE.equals(type) || DataTypes.FLOAT.equals(type) || DataTypes.DECIMAL.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("double");
		} else if (DataTypes.ASCII.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("character");
		} else if (DataTypes.DATE.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("date");
		} else if (DataTypes.TIME.equals(type) || DataTypes.TIMESTAMP.equals(type) || DataTypes.TIMEUUID.equals(type)) {
			modelDataType = uNoSQLFactory.createPrimitiveType("character");
		}

		return modelDataType;
	}
}

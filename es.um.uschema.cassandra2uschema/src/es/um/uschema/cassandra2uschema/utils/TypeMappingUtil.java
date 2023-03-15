package es.um.uschema.cassandra2uschema.utils;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.internal.core.type.DefaultSetType;

import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.utils.USchemaFactory;

public class TypeMappingUtil {

	public static DataType getType(com.datastax.oss.driver.api.core.type.DataType type, USchemaFactory uSchemaFactory) {
		DataType modelDataType = null;

		if (type instanceof com.datastax.oss.driver.internal.core.type.PrimitiveType) {
			modelDataType = createPrimitiveType(type, modelDataType, uSchemaFactory);
		} else if (type instanceof com.datastax.oss.driver.internal.core.type.DefaultListType) {
			com.datastax.oss.driver.internal.core.type.DefaultListType defaultListType = (com.datastax.oss.driver.internal.core.type.DefaultListType) type;
			DataType elementType = getType(defaultListType.getElementType(), uSchemaFactory);
			modelDataType = uSchemaFactory.createPList(elementType);

		} else if (type instanceof com.datastax.oss.driver.internal.core.type.DefaultMapType) {
			com.datastax.oss.driver.internal.core.type.DefaultMapType defaultMapType = (com.datastax.oss.driver.internal.core.type.DefaultMapType) type;
			DataType keyType = getType(defaultMapType.getKeyType(), uSchemaFactory);
			DataType valueType = getType(defaultMapType.getValueType(), uSchemaFactory);

			if (keyType instanceof PrimitiveType)
				modelDataType = uSchemaFactory.createPMap((PrimitiveType) keyType, valueType);
		} else if (type instanceof DefaultSetType) {
			DefaultSetType defaultSetType = (DefaultSetType) type;
			DataType elementType = getType(defaultSetType.getElementType(), uSchemaFactory);
			modelDataType = uSchemaFactory.createPSet(elementType);
		}

		return modelDataType;
	}

	private static DataType createPrimitiveType(com.datastax.oss.driver.api.core.type.DataType type, DataType modelDataType, USchemaFactory uSchemaFactory) {
		if (DataTypes.TEXT.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("string");
		} else if (DataTypes.INT.equals(type) || DataTypes.SMALLINT.equals(type) || DataTypes.TINYINT.equals(type) || DataTypes.VARINT.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("integer");
		} else if (DataTypes.BIGINT.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("long");
		} else if (DataTypes.BOOLEAN.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("boolean");
		} else if (DataTypes.DOUBLE.equals(type) || DataTypes.FLOAT.equals(type) || DataTypes.DECIMAL.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("double");
		} else if (DataTypes.ASCII.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("character");
		} else if (DataTypes.DATE.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("date");
		} else if (DataTypes.TIME.equals(type) || DataTypes.TIMESTAMP.equals(type) || DataTypes.TIMEUUID.equals(type)) {
			modelDataType = uSchemaFactory.createPrimitiveType("character");
		}

		return modelDataType;
	}
}

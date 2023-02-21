package es.um.unosql.hbase2unosql.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hadoop.hbase.util.Bytes;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class TypeInference {
	private static enum Type {
		STRING, BOOLEAN, BYTE, SHORT, CHAR, INT, FLOAT, DOUBLE, LONG, DATE, BINARY, UNKNOWN
	};

	private static final TypeInference INSTANCE = new TypeInference();

	private TypeInference() {
		try {
			asciiPattern = Pattern.compile("^\\p{ASCII}+$");
			model = (J48) weka.core.SerializationHelper.read(this.getClass().getResourceAsStream("/j48.model"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static TypeInference getInstance() {
		return INSTANCE;
	}

	private J48 model;
	private Pattern asciiPattern;

	private final static List<String> BOOLEAN_VALUES = Arrays.asList(new String[] { "false", "true" });
	private final static List<String> TYPE_VALUES = Arrays.asList(new String[] { "string", "boolean", "byte", "short",
			"char", "int", "float", "double", "long", "timestamp", "binary" });
	private static final byte RANGE_BYTE_MIN = Byte.MIN_VALUE;
	private static final byte RANGE_BYTE_MAX = Byte.MAX_VALUE;
	private static final char RANGE_CHAR_MIN = 33;
	private static final char RANGE_CHAR_MAX = 126;
	private static final long RANGE_TIMESTAMP_MIN = new GregorianCalendar(2000 + 1900, 01, 01).getTimeInMillis();
	private static final long RANGE_TIMESTAMP_MAX = new GregorianCalendar(2018 + 1900, 01, 01).getTimeInMillis();
	private static final int SIZE_BINARY_MIN = 256;
	private static final int SIZE_BINARY_MAX = 10 * 1024 * 1024; // 10 Mb

	private static ArrayList<Attribute> attributes = Stream
			.of(new Attribute[] { new Attribute("Printed_ASCII", BOOLEAN_VALUES), new Attribute("Length"),
					new Attribute("range_Byte", BOOLEAN_VALUES), new Attribute("range_Short", BOOLEAN_VALUES),
					new Attribute("range_Char", BOOLEAN_VALUES), new Attribute("range_Int", BOOLEAN_VALUES),
					new Attribute("range_Float", BOOLEAN_VALUES), new Attribute("range_Double", BOOLEAN_VALUES),
					new Attribute("range_Long", BOOLEAN_VALUES), new Attribute("range_Timestamp", BOOLEAN_VALUES),
					new Attribute("range_Image", BOOLEAN_VALUES),
					// Declare the class attribute
					new Attribute("class", TYPE_VALUES) })
			.collect(Collectors.toCollection(ArrayList::new));

	private boolean containsOnlyASCIICharacters(byte[] value) {
		String parsed = Bytes.toString(value);
		return asciiPattern.matcher(parsed).matches();
	}

	private boolean containsByte(byte[] value) {
		return value.length == 1 && value[0] >= RANGE_BYTE_MIN && value[0] <= RANGE_BYTE_MAX;
	}

	private boolean containsShort(byte[] value) {
		if (value.length == Short.BYTES)
			return true;
		return false;
	}

	private boolean containsChar(byte[] value) {
		if (value.length == 1) {
			char parsed = (char) value[0];
			return parsed >= RANGE_CHAR_MIN && parsed <= RANGE_CHAR_MAX;
		}
		return false;
	}

	private boolean containsInt(byte[] value) {
		if (value.length == Integer.BYTES)
			return true;
		return false;
	}

	private boolean containsFloat(byte[] value) {
		if (value.length == Float.BYTES)
			return true;
		return false;
	}

	private boolean containsDouble(byte[] value) {
		if (value.length == Double.BYTES)
			return true;
		return false;
	}

	private boolean containsLong(byte[] value) {
		if (value.length == Long.BYTES)
			return true;
		return false;
	}

	private boolean containsTimestamp(byte[] value) {
		if (value.length == Long.BYTES) {
			long parsed = Bytes.toLong(value);
			return parsed >= RANGE_TIMESTAMP_MIN && parsed <= RANGE_TIMESTAMP_MAX;
		}
		return false;
	}

	private boolean containsBinary(byte[] value) {
		return value.length >= SIZE_BINARY_MIN && value.length <= SIZE_BINARY_MAX;
	}

	private boolean containsBoolean(byte[] value) {
		return value.length == 1 && (value[0] == 1 || value[0] == 0);
	}

	public double[] getPredictors(byte[] bytes) {

		return new double[] { containsOnlyASCIICharacters(bytes) ? 1 : 0, // printed_ASCII
				bytes.length, // length
				containsByte(bytes) ? 1 : 0, // range_Byte
				containsShort(bytes) ? 1 : 0, // range_Short
				containsChar(bytes) ? 1 : 0, // range_Char
				containsInt(bytes) ? 1 : 0, // range_Int
				containsFloat(bytes) ? 1 : 0, // range_Float
				containsDouble(bytes) ? 1 : 0, // range_Double
				containsTimestamp(bytes) ? 1 : 0, // range_Timestamp
				containsLong(bytes) ? 1 : 0, // range_Long
				containsBinary(bytes) ? 1 : 0, // range_Image
				0 }; // class
	}

	protected String customInferenceStrategy(byte[] value) {

		Type inferred;
		if (containsBoolean(value))
			inferred = Type.BOOLEAN;
		else if (containsChar(value))
			inferred = Type.LONG;
		else if (containsByte(value))
			inferred = Type.LONG;
		else if (containsShort(value))
			inferred = Type.LONG;
		else if (containsInt(value))
			inferred = Type.LONG;
		else if (containsFloat(value))
			inferred = Type.DOUBLE;
		else if (containsLong(value))
			inferred = Type.LONG;
		else if (containsDouble(value))
			inferred = Type.DOUBLE;
		else if (containsTimestamp(value))
			inferred = Type.DATE;
		else if (containsOnlyASCIICharacters(value))
			inferred = Type.STRING;
		else
			inferred = Type.BINARY;

		return inferred.toString();
	}

	protected String tfgInferenceStrategy(byte[] value) {
		Instances test = new Instances("Types", attributes, 1);
		test.setClassIndex(test.numAttributes() - 1);
		DenseInstance instance = new DenseInstance(1.0, getPredictors(value));
		instance.setDataset(test);
		int prediction;
		try {
			prediction = (int) model.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
			return Type.UNKNOWN.toString();
		}
		Type type = Type.values()[prediction];

		switch (type) {
		case BYTE:
			return Type.LONG.toString();
		case SHORT:
			return Type.LONG.toString();
		case INT:
			return Type.LONG.toString();
		case FLOAT:
			return Type.LONG.toString();
		default:
			return type.toString();
		}
	}

	public String inferSingleValueType(byte[] value) {
//		return tfgInferenceStrategy(value);
		return customInferenceStrategy(value);
	}

}

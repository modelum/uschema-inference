package es.um.unosql.mongodb2unosql.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import es.um.unosql.mongodb2unosql.utils.Constants;
import es.um.unosql.mongodb2unosql.utils.Helpers;

class SimplificationTest {

	
	@Test
	public void testDocumentsWithDifferentFieldsAreNotEqual() {
		Document source = new Document();
		Document target = new Document();
		source.put("test1", "test");
		target.put("test2", Constants.SIMPLE_DEFAULT_STRING);
		assertNotEquals(Helpers.simplify(source), target);
	}
	
	@Test
	public void testSimpleDocumentWithString() {
		Document source = new Document();
		Document target = new Document();
		source.put("test", "test");
		target.put("test", Constants.SIMPLE_DEFAULT_STRING);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithInteger() {
		Document source = new Document();
		Document target = new Document();
		source.put("test", 42);
		target.put("test", Constants.SIMPLE_DEFAULT_INTEGER);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithLong() {
		Document source = new Document();
		Document target = new Document();
		source.put("test", 11231L);
		target.put("test", Constants.SIMPLE_DEFAULT_LONG);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithBoolean() {
		Document source = new Document();
		Document target = new Document();
		source.put("test", true);
		target.put("test", Constants.SIMPLE_DEFAULT_BOOLEAN);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithDouble() {
		Document source = new Document();
		Document target = new Document();
		source.put("test", 14.32);
		target.put("test", Constants.SIMPLE_DEFAULT_DOUBLE);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithObjectId() {
		Document source = new Document();
		Document target = new Document();
		source.put("_id", new ObjectId("78a2a17ecb1cbc4e15afd314"));
		target.put("_id", Constants.SIMPLE_DEFAULT_OBJECTID);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithHomogeneousArray() {
		ArrayList<String> sourceArray = new ArrayList<String>();
		ArrayList<String> targetArray = new ArrayList<String>();
		sourceArray.add("test1");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test2");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test3");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test4");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);

		Document source = new Document();
		Document target = new Document();
		source.put("array", sourceArray);
		target.put("array", targetArray);
		assertEquals(Helpers.simplify(source), target);
	}
	
	@Test
	public void testDocumentsWithDifferentArrayLengthsAreNotEqual() {
		ArrayList<String> sourceArray = new ArrayList<String>();
		ArrayList<String> targetArray = new ArrayList<String>();
		sourceArray.add("test1");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test2");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test3");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add("test4");

		Document source = new Document();
		Document target = new Document();
		source.put("array", sourceArray);
		target.put("array", targetArray);
		assertNotEquals(Helpers.simplify(source), target);
	}
	

	@Test
	public void testSimpleDocumentWithHeterogeneousArray() {

		ArrayList<Object> sourceArray = new ArrayList<Object>();
		ArrayList<Object> targetArray = new ArrayList<Object>();
		sourceArray.add("test1");
		targetArray.add(Constants.SIMPLE_DEFAULT_STRING);
		sourceArray.add(1);
		targetArray.add(Constants.SIMPLE_DEFAULT_INTEGER);
		sourceArray.add(true);
		targetArray.add(Constants.SIMPLE_DEFAULT_BOOLEAN);
		sourceArray.add(1.43);
		targetArray.add(Constants.SIMPLE_DEFAULT_DOUBLE);

		Document source = new Document();
		Document target = new Document();
		source.put("array", sourceArray);
		target.put("array", targetArray);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testSimpleDocumentWithEmbeddedDocument() {

		Document sourceDocument = new Document();
		Document targetDocument = new Document();
		sourceDocument.put("test", 11231L);
		targetDocument.put("test", Constants.SIMPLE_DEFAULT_LONG);

		Document source = new Document();
		Document target = new Document();
		source.put("doc", sourceDocument);
		target.put("doc", targetDocument);
		assertEquals(Helpers.simplify(source), target);
	}

	@Test
	public void testComplexDocument() {
		Document sourceDocument1 = new Document();
		Document targetDocument1 = new Document();
		sourceDocument1.put("name", "none");
		targetDocument1.put("name", Constants.SIMPLE_DEFAULT_STRING);
		sourceDocument1.put("lastname", "other");
		targetDocument1.put("lastname", Constants.SIMPLE_DEFAULT_STRING);
		sourceDocument1.put("age", 44);
		targetDocument1.put("age", Constants.SIMPLE_DEFAULT_INTEGER);
		
		Document sourceDocument2 = new Document();
		Document targetDocument2 = new Document();
		sourceDocument2.put("address", "Fake Street");
		targetDocument2.put("address", Constants.SIMPLE_DEFAULT_STRING);
		sourceDocument2.put("number", 123);
		targetDocument2.put("number", Constants.SIMPLE_DEFAULT_INTEGER);
		sourceDocument2.put("zipcode", "43122");
		targetDocument2.put("zipcode", Constants.SIMPLE_DEFAULT_STRING);
		
		Document source = new Document();
		Document target = new Document();
		source.put("_id", new ObjectId("78a2a17ecb1cbc4e15afd314"));
		target.put("_id", Constants.SIMPLE_DEFAULT_OBJECTID);
		source.put("person", sourceDocument1);
		target.put("person", targetDocument1);
		source.put("residence", sourceDocument2);
		target.put("residence", targetDocument2);
		
		assertEquals(Helpers.simplify(source), target);
	}
}

package es.um.unosql.mongodb2unosql.test;

import static org.junit.jupiter.api.Assertions.*;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import es.um.unosql.mongodb2unosql.utils.Constants;
import es.um.unosql.mongodb2unosql.utils.Helpers;
import scala.Tuple2;
import scala.Tuple3;

class PairOperationsTest {

	@Test
	void testGenerateDocumentPair() {
		
		long timestamp = 1573471847;
		Document sourceDocument = new Document();
		Document targetDocument = new Document();
		ObjectId sourceObjectId = new ObjectId((int)timestamp, 13000);
		sourceDocument.put("_id", sourceObjectId);
		targetDocument.put("_id", Constants.SIMPLE_DEFAULT_OBJECTID);
		sourceDocument.put("longval", 11231L);
		targetDocument.put("longval", Constants.SIMPLE_DEFAULT_LONG);
		sourceDocument.put("stringval", "hello");
		targetDocument.put("stringval", Constants.SIMPLE_DEFAULT_STRING);
		Tuple2<Document, Tuple3<Long, Long, Integer>> expected = 
				new Tuple2<Document, Tuple3<Long,Long,Integer>>
				(targetDocument, 
				 new Tuple3<Long, Long, Integer> (timestamp, timestamp, 1));
		
		assertEquals(expected, Helpers.generateDocumentPair(sourceDocument));		
	}
	
	@Test
	void testMergePairs() {
		
		long firstTimestamp1 = 1573471847;
		long firstTimestamp2 = 1473471847;
		long lastTimestamp1 = 1773471847;
		long lastTimestamp2 = 1673471847;
		int count1 = 10;
		int count2 = 12;
		
		Document document = new Document();
		document.put("_id", Constants.SIMPLE_DEFAULT_OBJECTID);
		document.put("longval", Constants.SIMPLE_DEFAULT_LONG);
		document.put("stringval", Constants.SIMPLE_DEFAULT_STRING);
		
		Tuple2<Document, Tuple3<Long, Long, Integer>> source1 = 
				new Tuple2<Document, Tuple3<Long,Long,Integer>>
				(document, new Tuple3<Long, Long, Integer> (firstTimestamp1, lastTimestamp1, count1));
		
		Tuple2<Document, Tuple3<Long, Long, Integer>> source2 = 
				new Tuple2<Document, Tuple3<Long,Long,Integer>>
				(document, 
				 new Tuple3<Long, Long, Integer> (firstTimestamp2, lastTimestamp2, count2));
		
		Tuple2<Document, Tuple3<Long, Long, Integer>> expected = 
				new Tuple2<Document, Tuple3<Long,Long,Integer>>
				(document, 
				 new Tuple3<Long, Long, Integer> (firstTimestamp2, lastTimestamp1, count1 + count2));
		
		assertEquals(expected._2, Helpers.reducePairs(source1._2, source2._2));		
	}
}

package es.um.uschema.doc2uschema.validation.queries;

import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.StructuralVariation;

public class MongoDBQueryExecutor
{
	private MongoDatabase database;

	public MongoDBQueryExecutor(MongoClient mongoClient, String databaseName) {
		this.database = mongoClient.getDatabase(databaseName);
	}

	public FindIterable<Document> getAll(StructuralVariation structuralVariation) {
		String collectionName = structuralVariation.getContainer().getName();
		
		MongoCollection<Document> collection = database.getCollection(collectionName);
		System.out.println("Currently quering: Entity [" + collectionName + " - Variation id: " + structuralVariation.getVariationId() + "]");
		
		List<Bson> filterList = structuralVariation.getStructuralFeatures().stream().filter(Attribute.class::isInstance).map(Attribute.class::cast).map(sf -> exists(sf.getName())).collect(Collectors.toList());
		Bson filters = Filters.and(filterList);
		
		return collection.find(filters);
	}
}

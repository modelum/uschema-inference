package es.um.unosql.doc2unosql.validation.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.doc2unosql.validation.queries.checkers.DocumentChecker;
import es.um.unosql.doc2unosql.validation.queries.statistics.StatisticsMeter;
import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class MongoDBValidator {
	private MongoClient mongoClient;
	private StatisticsMeter statisticsMeter;
	private String databaseName;
	private Map<StructuralVariation, Integer> aggregatedVariationsCounter;

	public MongoDBValidator(String databaseUri, String port, String databaseName) {
		this.mongoClient = MongoClients.create("mongodb://" + databaseUri + ":" + port);
		this.databaseName = databaseName;
		this.statisticsMeter = new StatisticsMeter();
		this.aggregatedVariationsCounter = new HashMap<>();
	}

	public void validate(uNoSQLSchema uNoSQLSchema) {
		uNoSQLSchema.getEntities().stream().filter(e -> e.isRoot()).forEach(et -> {
			et.getVariations().forEach(sv -> {
				performQuery(sv);
			});
		});
		statisticsMeter.saveStructuralVariationStatistics(aggregatedVariationsCounter);
	}

	private List<Document> performQuery(StructuralVariation structuralVariation) {
		MongoDBQueryExecutor mongoDBQueryExecutor = new MongoDBQueryExecutor(mongoClient, databaseName);
		FindIterable<Document> queryResultDocuments = mongoDBQueryExecutor.getAll(structuralVariation);
		
		DocumentChecker propertiesDocumentReductor = new DocumentChecker(structuralVariation, aggregatedVariationsCounter);
		List<Document> resultDocuments = propertiesDocumentReductor.reduceVariationDocuments(queryResultDocuments);

		statisticsMeter.saveStructuralVariationStatistics(structuralVariation, resultDocuments);

		return resultDocuments;
	}

	public String getStatisticsAsString() {
		if (statisticsMeter == null) {
			return null;
		}

		return statisticsMeter.getStatisticsAsString();
	}

}
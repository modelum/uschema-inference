package es.um.uschema.hbase2uschema.validation.validation.queries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;

import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.hbase2uschema.validation.queries.checkers.RowChecker;
import es.um.uschema.hbase2uschema.validation.queries.exceptions.DuplicatedIdException;
import es.um.uschema.hbase2uschema.validation.queries.statistics.StatisticsMeter;

public class HBaseValidator {

	private static final String HBASE_CONNECTION_HOST = "hbase.zookeeper.quorum";
	private static final String HBASE_CONNECTION_PORT = "hbase.zookeeper.property.clientPort";
	private static final String HBASE_CONNECTION_TIMEOUT = "timeout";

	private StatisticsMeter statisticsMeter;
	private Configuration config;
	private Set<String> ids;
	
	public HBaseValidator(String databaseUri, int port) {
		this.config = HBaseConfiguration.create();
		this.config.set(HBASE_CONNECTION_HOST, "127.0.0.1");
		this.config.setInt(HBASE_CONNECTION_PORT, port);
		this.config.setInt(HBASE_CONNECTION_TIMEOUT, 60000);
		this.statisticsMeter = new StatisticsMeter();
	}

	public void validate(USchema uSchema) {
	  uSchema.getEntities().stream().filter(e -> e.isRoot()).forEach(et -> {
			et.getVariations().forEach(sv -> {
				this.ids = new HashSet<>();
				performQuery(sv);
			});
		});
	}

	private List<Result> performQuery(StructuralVariation structuralVariation) {
		HBaseQueryExecutor mongoDBQueryExecutor = new HBaseQueryExecutor(config);
		List<Result> rowResults = mongoDBQueryExecutor.getAll(structuralVariation);
		
		RowChecker propertiesDocumentReductor = new RowChecker(structuralVariation);
		List<Result> reduceVariationRows = propertiesDocumentReductor.reduceVariationRows(rowResults);

		checkIds(structuralVariation, reduceVariationRows);
		statisticsMeter.saveStructuralVariationStatistics(structuralVariation, reduceVariationRows);

		return rowResults;
	}

	private void checkIds(StructuralVariation structuralVariation, List<Result> rowsResults) {
		rowsResults.forEach(row -> {
			String id = new String(row.getRow());
			boolean added = ids.add(id);
			if (!added) {
				System.err.println("Duplicated ID as result of different queries: " + id);
				throw new DuplicatedIdException("Duplicated ID as result of different queries: " + id);
			}
		});
	}

	public String getStatisticsAsString() {
		if (statisticsMeter == null) {
			return null;
		}

		return statisticsMeter.getStatisticsAsString();
	}

}
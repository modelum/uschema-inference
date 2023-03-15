package es.um.uschema.doc2uschema.validation.queries.statistics;

import static es.um.uschema.doc2uschema.validation.queries.constants.Constants.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;

import es.um.uschema.USchema.StructuralVariation;

public class StatisticsMeter {

	private List<StatisticResult> results;

	public StatisticsMeter() {
		this.results = new LinkedList<StatisticResult>();
	}

	public void saveStructuralVariationStatistics(StructuralVariation structuralVariation, List<Document> resultDocuments) {
		StatisticResult statisticResult = new StatisticResult(structuralVariation, resultDocuments);
		results.add(statisticResult);
	}

	public String getStatisticsAsString() {
		String header = "Entity" + TAB + TAB + "Variation Id" + TAB + "Result" + TAB + "Obtained" + TAB + "Expected"
				+ TAB + "Properties" + TAB + "References" + LINE;
		String resultData = results.stream().map(r -> r.getStatisticsAsString()).collect(Collectors.joining(LINE));

		return header + resultData;
	}

	public void saveStructuralVariationStatistics(Map<StructuralVariation, Integer> aggregatedVariationsCounter) {
		aggregatedVariationsCounter.forEach((structuralVariation, count) -> {
			StatisticResult statisticResult = new StatisticResult(structuralVariation, count);
			results.add(statisticResult);
		});
	}

}

package es.um.unosql.redis2unosql.validation.queries.statistics;

import static es.um.unosql.redis2unosql.validation.queries.constants.Constants.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class StatisticsMeter {

	private List<StatisticResult> results;
	private Map<StructuralVariation, StatisticResult> variationsResults;

	public StatisticsMeter() {
		this.results = new LinkedList<StatisticResult>();
		this.variationsResults = new HashMap<StructuralVariation, StatisticResult>();
	}

	public void saveStructuralVariationStatistics(StructuralVariation structuralVariation, int resultNumber) {
		StatisticResult statisticResult = variationsResults.get(structuralVariation);
		if (statisticResult == null) {
			statisticResult = new StatisticResult(structuralVariation, resultNumber);
			variationsResults.put(structuralVariation, statisticResult);
			results.add(statisticResult);
		}
	}

	public String getStatisticsAsString() {
		String header = "Entity" + TAB + TAB + "Variation Id" + TAB + "Result" + TAB + "Obtained" + TAB + "Expected"
				+ TAB + "Properties" + TAB + "References" + LINE;
		String resultData = results.stream().map(r -> r.getStatisticsAsString()).collect(Collectors.joining(LINE));

		return header + resultData;
	}

}

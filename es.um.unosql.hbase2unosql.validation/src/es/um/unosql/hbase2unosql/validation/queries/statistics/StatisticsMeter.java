package es.um.unosql.hbase2unosql.validation.queries.statistics;

import static es.um.unosql.hbase2unosql.validation.queries.constants.Constants.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.client.Result;

import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class StatisticsMeter
{

	private List<StatisticResult> results;
	
	public StatisticsMeter()
	{
		this.results = new LinkedList<StatisticResult>();
	}
	
	public void saveStructuralVariationStatistics(StructuralVariation structuralVariation, 
			List<Result> rowResults)
	{
		StatisticResult statisticResult = new StatisticResult(structuralVariation, rowResults);
		results.add(statisticResult);		
	}

	public String getStatisticsAsString()
	{
		String header = "Entity" + TAB + TAB + "Variation Id" + TAB + "Result"  + TAB + "Obtained" + TAB + "Expected" + TAB + "Properties" + TAB + "References" + LINE;
		String resultData = results.stream().map(r -> r.getStatisticsAsString()).collect(Collectors.joining(LINE));
		
		return header + resultData;
	}

}

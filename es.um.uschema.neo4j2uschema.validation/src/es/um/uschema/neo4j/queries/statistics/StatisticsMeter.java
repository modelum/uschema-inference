package es.um.uschema.neo4j.queries.statistics;

import static es.um.uschema.neo4j.queries.constants.Constants.*;

import java.util.LinkedList;
import java.util.List;

import org.apache.spark.sql.Row;

import es.um.uschema.USchema.StructuralVariation;

public class StatisticsMeter
{

	private List<StatisticResult> results;
	private boolean findOne;
	
	public StatisticsMeter()
	{
		this.results = new LinkedList<StatisticResult>();
	}
	
	public void saveStructuralVariationStatistics(StructuralVariation structuralVariation, 
			List<Row> resultRows, String query)
	{
		StatisticResult statisticResult = new StatisticResult(structuralVariation, resultRows, query);
		results.add(statisticResult);		
	}

	public String getStatisticsAsString()
	{
		String result = "Result"  + TAB + "Obtained" + TAB + "Expected" + TAB + "Properties" + TAB + "References" + TAB + "Query" + LINE;
		
		for (StatisticResult statisticResult : results)
		{
			result += statisticResult.getStatisticsAsString(findOne) + LINE;
		}
		
		return result;
	}

	public void setFindOne(boolean findOne)
	{
		this.findOne = findOne;
	}

}

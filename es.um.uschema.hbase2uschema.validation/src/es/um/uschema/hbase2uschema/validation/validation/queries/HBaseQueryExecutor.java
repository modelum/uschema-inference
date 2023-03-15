package es.um.uschema.hbase2uschema.validation.validation.queries;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.StructuralVariation;

public class HBaseQueryExecutor {

	private Configuration config;

	public HBaseQueryExecutor(Configuration config) {
		this.config = config;
	}

	public List<Result> getAll(StructuralVariation structuralVariation) {
		List<Result> results = new LinkedList<Result>();
		try {
			HBaseAdmin.available(config);
			Connection connection = ConnectionFactory.createConnection(config);
			String tableName = structuralVariation.getContainer().getName();
			Table table = connection.getTable(TableName.valueOf(tableName));

			System.out.println("Currently quering: Entity[" + tableName + " - Variation id: " + structuralVariation.getVariationId() + "]");

			Scan s = new Scan();
//			FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
//			s.setFilter(filterList);
		
			ResultScanner scanner = table.getScanner(s);
			for (Result result : scanner) {
				List<String> familyColumnNames = result.getNoVersionMap().keySet().stream().map(k -> new String(k)).collect(Collectors.toList());
				boolean present = true;
				
				if (result.getNoVersionMap().keySet().size() == structuralVariation.getStructuralFeatures().stream().filter(Aggregate.class::isInstance).count()) {
					
					List<Aggregate> list = structuralVariation.getStructuralFeatures().stream().filter(Aggregate.class::isInstance).map(Aggregate.class::cast).collect(Collectors.toList());
					for (Aggregate aggregate : list) {
						if (!familyColumnNames.contains(aggregate.getName())) {
							present = false;
						}
					}
					
				}
				
				if (present) {
					results.add(result);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return results;
	}

}

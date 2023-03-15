package es.um.uschema.cassandra2uschema.spark.map;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.spark.api.java.function.Function;

import com.datastax.spark.connector.japi.CassandraRow;

public class CassandraRowToColumnListMapping implements Function<CassandraRow, SortedSet<String>>
{
	private static final long serialVersionUID = 2563722877842150992L;

	@Override
	public SortedSet<String> call(CassandraRow row) throws Exception
	{
		SortedSet<String> columns;
		Map<String, Object> rowMap = row.toMap();

		columns = rowMap.entrySet().stream().filter(e ->
			e.getValue() != null &&
			!(e.getValue() instanceof scala.collection.immutable.Nil) &&
			!(e.getValue() instanceof scala.collection.immutable.Set.EmptySet$) &&
			!(e.getValue() instanceof scala.collection.immutable.Map.EmptyMap$))
		.map(Map.Entry::getKey)
		.collect(Collectors.toCollection(TreeSet<String>::new));

		return columns;
	}
}

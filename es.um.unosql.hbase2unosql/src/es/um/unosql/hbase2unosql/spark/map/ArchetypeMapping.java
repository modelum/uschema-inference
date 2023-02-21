package es.um.unosql.hbase2unosql.spark.map;

import static es.um.unosql.hbase2unosql.constants.Constants.ENTITY;
import static es.um.unosql.hbase2unosql.constants.Constants.PROPERTIES;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class ArchetypeMapping
		implements PairFunction<Tuple2<ImmutableBytesWritable, Result>, TreeMap<String, Object>, Long> {

	private static final long serialVersionUID = 1L;

	private String entityName;

	public ArchetypeMapping(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public Tuple2<TreeMap<String, Object>, Long> call(Tuple2<ImmutableBytesWritable, Result> row) throws Exception {
		Result value = row._2;

		TreeMap<String, Object> schema = new TreeMap<>();
		schema.put(ENTITY, entityName);
		TreeMap<String, Object> properties = new TreeMap<>();
		schema.put(PROPERTIES, properties);

		value.getNoVersionMap().entrySet().forEach(familyColumn -> {
			String familyColumnName = Bytes.toString(familyColumn.getKey());

			TreeMap<String, Object> familyColumnMap = new TreeMap<>();
			properties.put(familyColumnName, familyColumnMap);

			familyColumnMap.put(ENTITY, familyColumnName);

			TreeMap<String, Object> familyColumnProperties = new TreeMap<>();
			familyColumnMap.put(PROPERTIES, familyColumnProperties);

			NavigableMap<byte[], byte[]> columns = familyColumn.getValue();
			columns.entrySet().stream().forEach(c -> {
				String columnName = Bytes.toString(c.getKey());
				int colpos = columnName.indexOf(':');
				if (colpos != -1)
					columnName = columnName.substring(colpos + 1);

				familyColumnProperties.put(columnName, "s");
			});
		});

		return new Tuple2<TreeMap<String, Object>, Long>(schema, 1L);
	}

}

package es.um.uschema.neo4j2uschema.spark.map;

import static es.um.uschema.neo4j2uschema.constants.Constants.CLOSING_SQUARE_BRACKETS;
import static es.um.uschema.neo4j2uschema.constants.Constants.COMMA;
import static es.um.uschema.neo4j2uschema.constants.Constants.DOUBLE_QUOTE_ESCAPED;
import static es.um.uschema.neo4j2uschema.constants.Constants.ENTITY;
import static es.um.uschema.neo4j2uschema.constants.Constants.LABELS;
import static es.um.uschema.neo4j2uschema.constants.Constants.NODE;
import static es.um.uschema.neo4j2uschema.constants.Constants.OPENING_SQUARE_BRACKETS;
import static es.um.uschema.neo4j2uschema.constants.Constants.PROPERTIES;
import static es.um.uschema.neo4j2uschema.constants.Constants.REFERENCES;
import static es.um.uschema.neo4j2uschema.constants.Constants.REFS_TO;
import static es.um.uschema.neo4j2uschema.constants.Constants.RELATIONSHIP;
import static es.um.uschema.neo4j2uschema.constants.Constants.TYPE;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Row;
import org.neo4j.driver.types.Entity;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import es.um.uschema.neo4j2uschema.utils.TypeUtils;
import scala.Tuple2;

public class IdArchetypeMapping implements PairFunction<Row, Long, TreeMap<String, Object>> {
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<Long, TreeMap<String, Object>> call(Row row) throws Exception {
		Node node = (Node) row.get(0);
		Relationship relationship = null;
		List<String> targetLabels = null;
		if (!row.isNullAt(1) && !row.isNullAt(2)) {
			relationship = (Relationship) row.get(1);
			Object[] labelsObjectList = (Object[]) row.get(2);
			targetLabels = new LinkedList<String>();
			for (Object o : labelsObjectList) {
				targetLabels.add((String) o);
			}
		}

		TreeMap<String, Object> jsonNode = nodeToJSONObject(node, relationship, targetLabels);

		return new Tuple2<Long, TreeMap<String, Object>>(node.id(), jsonNode);
	}

	private TreeMap<String, Object> nodeToJSONObject(Node node, Relationship relationship, List<String> targetLabels) {
		TreeMap<String, Object> result = new TreeMap<String, Object>();
		result.put(LABELS,
				OPENING_SQUARE_BRACKETS + StreamSupport.stream(node.labels().spliterator(), false)
						.map(l -> DOUBLE_QUOTE_ESCAPED + l + DOUBLE_QUOTE_ESCAPED).sorted()
						.collect(Collectors.joining(",")) + CLOSING_SQUARE_BRACKETS);
		result.put(ENTITY, NODE);
		addProperties(node, result);
		addRelationships(relationship, targetLabels, result);

		return result;
	}

	private void addProperties(Entity entity, TreeMap<String, Object> jsonObject) {
		TreeMap<String, Object> properties = new TreeMap<String, Object>();
		entity.keys().forEach(k -> {
			Object type = TypeUtils.obtainType(entity.get(k));
			properties.put(k, type.getClass().isArray()
					? OPENING_SQUARE_BRACKETS + Stream.of((Object[]) type)
							.map(e -> e instanceof String ? DOUBLE_QUOTE_ESCAPED + e.toString() + DOUBLE_QUOTE_ESCAPED
									: e.toString())
							.collect(Collectors.joining(COMMA)) + CLOSING_SQUARE_BRACKETS
					: type);
		});
		jsonObject.put(PROPERTIES, properties);
	}

	private class TreeMapComparator implements Comparator<TreeMap<String, Object>>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(TreeMap<String, Object> e1, TreeMap<String, Object> e2) {
			return e1.toString().compareTo(e2.toString());
		}

	}

	private void addRelationships(Relationship rel, List<String> targetLabels, TreeMap<String, Object> jsonNode) {
		TreeSet<TreeMap<String, Object>> references = new TreeSet<TreeMap<String, Object>>(new TreeMapComparator());
		if (rel != null && targetLabels != null) {
			TreeMap<String, Object> relationship = new TreeMap<String, Object>();

			relationship.put(TYPE, rel.type());
			relationship.put(REFS_TO,
					OPENING_SQUARE_BRACKETS
							+ targetLabels.stream().map(l -> DOUBLE_QUOTE_ESCAPED + l + DOUBLE_QUOTE_ESCAPED)
									.collect(Collectors.joining(COMMA))
							+ CLOSING_SQUARE_BRACKETS);
			relationship.put(ENTITY, RELATIONSHIP);
			addProperties(rel, relationship);

			references.add(relationship);
		}
		jsonNode.put(REFERENCES, references);
	}

}

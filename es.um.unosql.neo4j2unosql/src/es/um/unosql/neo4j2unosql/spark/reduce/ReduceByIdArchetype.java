package es.um.unosql.neo4j2unosql.spark.reduce;

import static es.um.unosql.neo4j2unosql.constants.Constants.REFERENCES;

import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.spark.api.java.function.Function2;

public class ReduceByIdArchetype
		implements Function2<TreeMap<String, Object>, TreeMap<String, Object>, TreeMap<String, Object>> {
	private static final long serialVersionUID = 1L;

	@Override
	@SuppressWarnings("unchecked")
	public TreeMap<String, Object> call(TreeMap<String, Object> nodeString1, TreeMap<String, Object> nodeString2)
			throws Exception {
		TreeSet<TreeMap<String, Object>> references = (TreeSet<TreeMap<String, Object>>) nodeString1.get(REFERENCES);
		TreeSet<TreeMap<String, Object>> references2 = (TreeSet<TreeMap<String, Object>>) nodeString2.get(REFERENCES);
		references.addAll(references2);
		
		return nodeString1; 
	}
}
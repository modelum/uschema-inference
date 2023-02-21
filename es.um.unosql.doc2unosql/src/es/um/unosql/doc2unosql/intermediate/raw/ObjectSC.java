package es.um.unosql.doc2unosql.intermediate.raw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.um.unosql.doc2unosql.metadata.ObjectMetadata;

public class ObjectSC extends SchemaComponent {
	private List<Map.Entry<String, SchemaComponent>> inners;

	public Boolean isRoot;
	public ObjectMetadata meta;
	public String entityName;

	public ObjectSC() {
		inners = new ArrayList<Map.Entry<String, SchemaComponent>>(20);
		isRoot = Boolean.FALSE;
	}

	@Override
	public int hashCode() {
		return entityName.hashCode() ^ isRoot.hashCode() ^ inners.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ObjectSC) {
			ObjectSC theObject = (ObjectSC) other;
			return entityName.equals(theObject.entityName) && isRoot.equals(theObject.isRoot)
					&& inners.equals(theObject.inners);
		}

		return false;
	}

	public void addAll(Collection<Map.Entry<String, SchemaComponent>> elements) {
		inners.addAll(elements);
	}

	public void addAll(Iterable<Map.Entry<String, SchemaComponent>> elements) {
		for (Map.Entry<String, SchemaComponent> p : elements)
			inners.add(p);
	}

	public void add(Map.Entry<String, SchemaComponent> sc) {
		inners.add(sc);
	}

	public List<Map.Entry<String, SchemaComponent>> getInners() {
		return inners;
	}

	public int size() {
		return inners.size();
	}
}

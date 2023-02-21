package es.um.unosql.doc2unosql.process.util;

import java.util.List;
import java.util.Map;

import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;

public interface EVariationMerger
{
	void mergeEquivalentEVs(Map<String, List<SchemaComponent>> rawEntities);
}

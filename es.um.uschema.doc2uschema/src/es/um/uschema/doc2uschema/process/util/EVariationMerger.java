package es.um.uschema.doc2uschema.process.util;

import java.util.List;
import java.util.Map;

import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;

public interface EVariationMerger
{
	void mergeEquivalentEVs(Map<String, List<SchemaComponent>> rawEntities);
}

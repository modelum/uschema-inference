package es.um.uschema.doc2uschema.process.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;

public interface AliasedAggregatedEntityJoiner 
{
	void joinAggregatedEntities(Map<String, List<SchemaComponent>> rawEntities, Set<String> innerSchemaNames);
}
package es.um.unosql.doc2unosql.process.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;

public interface AliasedAggregatedEntityJoiner 
{
	void joinAggregatedEntities(Map<String, List<SchemaComponent>> rawEntities, Set<String> innerSchemaNames);
}
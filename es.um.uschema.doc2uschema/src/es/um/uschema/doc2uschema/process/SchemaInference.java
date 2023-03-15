package es.um.uschema.doc2uschema.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.inject.Inject;

import es.um.uschema.doc2uschema.intermediate.raw.ArraySC;
import es.um.uschema.doc2uschema.intermediate.raw.BooleanSC;
import es.um.uschema.doc2uschema.intermediate.raw.NullSC;
import es.um.uschema.doc2uschema.intermediate.raw.NumberSC;
import es.um.uschema.doc2uschema.intermediate.raw.ObjectIdSC;
import es.um.uschema.doc2uschema.intermediate.raw.ObjectSC;
import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;
import es.um.uschema.doc2uschema.intermediate.raw.StringSC;
import es.um.uschema.doc2uschema.intermediate.raw.util.SchemaPrinter;
import es.um.uschema.doc2uschema.metadata.ObjectMetadata;
import es.um.uschema.doc2uschema.process.util.AliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.EVariationMerger;
import es.um.uschema.doc2uschema.process.util.MakePair;
import es.um.uschema.doc2uschema.util.abstractjson.IAJArray;
import es.um.uschema.doc2uschema.util.abstractjson.IAJBoolean;
import es.um.uschema.doc2uschema.util.abstractjson.IAJElement;
import es.um.uschema.doc2uschema.util.abstractjson.IAJNull;
import es.um.uschema.doc2uschema.util.abstractjson.IAJNumber;
import es.um.uschema.doc2uschema.util.abstractjson.IAJObject;
import es.um.uschema.doc2uschema.util.abstractjson.IAJObjectId;
import es.um.uschema.doc2uschema.util.abstractjson.IAJTextual;
import es.um.uschema.doc2uschema.util.config.SchemaInferenceConfig;
import es.um.uschema.doc2uschema.util.inflector.Inflector;

public class SchemaInference
{
	@Inject
	private EVariationMerger merger;

	@Inject
	private AliasedAggregatedEntityJoiner joiner;

	@Inject
	private SchemaInferenceConfig config;

	private IAJArray docArray;
	private Map<String, List<SchemaComponent>> rawEntities;
	private Set<String> innerSchemaNames;

	private enum ROOT_TYPE {ROOT_OBJECT, NON_ROOT_OBJECT}
	private enum DEBUG_TYPE {DEBUG, NO_DEBUG}

	private static final DEBUG_TYPE debug_type = DEBUG_TYPE.NO_DEBUG;

	public SchemaInference()
	{
		docArray = null;
		rawEntities = new HashMap<String, List<SchemaComponent>>();
		innerSchemaNames = new HashSet<String>();
	}

	public SchemaInference(EVariationMerger merger, AliasedAggregatedEntityJoiner joiner, SchemaInferenceConfig config)
	{
	  this();
	  this.merger = merger;
	  this.joiner = joiner;
	  this.config = config;
	}

	private boolean validateRows(IAJArray rows)
	{
		// Check just the first element, suppose the rest are correct, as this will be the result of some automated process
		Iterator<IAJElement> iterator = rows.iterator();
		if (!iterator.hasNext())
			return true; // Empty

		IAJObject triple = iterator.next().asObject();
		return Optional.ofNullable(triple.get("schema")).filter(IAJElement::isObject).isPresent() &&
				Optional.ofNullable(triple.get("count")).filter(IAJElement::isNumber).isPresent() &&
				Optional.ofNullable(triple.get("firstTimestamp")).filter(IAJElement::isNumber).isPresent() &&
				Optional.ofNullable(triple.get("lastTimestamp")).filter(IAJElement::isNumber).isPresent();
	}

	private void innerCountAndTimestampsAdjust(Set<String> innerSchemaNames, Map<String, List<SchemaComponent>> rawEntities)
	{
		// FIXME: I'm not sure this will work for n levels of aggregation

		List<SchemaComponent> allSchemaComponents =
				rawEntities.values().stream().flatMap(List::stream).collect(Collectors.toList());

		// For each non-root entity...
		for (String innerSchema : innerSchemaNames)
		{
			// Each of these non-root objects have count and timestamp = 0
			for (SchemaComponent schComponent : rawEntities.get(innerSchema))
			{
				ObjectSC nonRootObj = (ObjectSC)schComponent;

				allSchemaComponents.stream()
					.forEach(sc -> {
						if (containsSchemaComponent((ObjectSC)sc, nonRootObj))
							nonRootObj.meta.combineMetadata(((ObjectSC)sc).meta);
				});
			}
		}
	}

	private boolean containsSchemaComponent(ObjectSC osc, ObjectSC nonRootObj)
	{
		return
			osc.getInners().stream().map(Map.Entry::getValue)
				.anyMatch(innerSchComponent ->
					(innerSchComponent instanceof ArraySC && ((ArraySC)innerSchComponent).getInners().contains(nonRootObj))
					|| (innerSchComponent instanceof ObjectSC && innerSchComponent.equals(nonRootObj)));
	}

	public Map<String, List<SchemaComponent>> infer(IAJArray rows)
	{
		if(!validateRows(rows))
			throw new IllegalArgumentException("JSON rows do not follow the expected schema: [ {schema: <JSON Object>, count: <Integer>, timestamp: <Long>} ...]");

		docArray = rows;

		docArray.forEach(n ->
			infer(n.get("schema"),
					Optional.empty(), ROOT_TYPE.ROOT_OBJECT,
					new ObjectMetadata(n.get("count").asLong(), n.get("firstTimestamp").asLong(), n.get("lastTimestamp").asLong()))
		);

		joiner.joinAggregatedEntities(rawEntities, innerSchemaNames);
		innerCountAndTimestampsAdjust(innerSchemaNames, rawEntities);
		merger.mergeEquivalentEVs(rawEntities);

		if (debug_type == DEBUG_TYPE.DEBUG)
			SchemaPrinter.schemaEntities(rawEntities);

		return rawEntities;
	}

	private SchemaComponent infer(IAJElement n, Optional<String> elementName, ROOT_TYPE rootObject, ObjectMetadata meta)
	{
		if (n.isObject())
			return infer(n.asObject(), elementName, rootObject, meta);

		if (n.isArray())
			return infer(n.asArray(), elementName.get());

		if (n.isBoolean())
			return infer(n.asBoolean(), elementName.get());

		if(n.isNumber())
			return infer(n.asNumber(), elementName.get());

		if (n.isNull())
			return infer(n.asNull(), elementName.get());

		if (n.isTextual())
			return infer(n.asTextual(), elementName.get());

		if (n.isObjectId())
			return infer(n.asObjectId(), elementName.get());

		assert(false);

		return null;
	}

	private SchemaComponent infer(IAJObject n, Optional<String> elementName, ROOT_TYPE isRoot, ObjectMetadata meta)
	{
		// Entity names are by convention capitalized
		Optional<String> typeName = Optional.empty();

		// TODO: Remember now a variation can't be root, it is entity which may be root.
		if (isRoot == ROOT_TYPE.ROOT_OBJECT)
			typeName = Optional.ofNullable(n.get(config.getTypeMarkerAttribute())).map(_n -> Inflector.getInstance().capitalize(_n.asString()));

		ObjectSC schema = new ObjectSC();
		schema.isRoot = isRoot == ROOT_TYPE.ROOT_OBJECT;
		schema.meta = meta;
		schema.entityName = typeName.orElse(Inflector.getInstance().capitalize(elementName.orElse("")));

		// Sorted so that the attributes of all the entities and variations are always in the same order
		Iterable<String> iterable = ()-> n.getFieldNames();
		SortedSet<String> fields =
				StreamSupport.stream(iterable.spliterator(),false).filter(f -> !config.getIgnoredAttributes().contains(f))
					.collect(Collectors.toCollection(TreeSet<String>::new));

		// Recursive phase
		schema.addAll(fields.stream()
				.map(f -> MakePair.of(f, infer(n.get(f), Optional.of(f), ROOT_TYPE.NON_ROOT_OBJECT, new ObjectMetadata())))::iterator);

		// Now that we have the complete schema, try to compare it with any of the variations in the map
		List<SchemaComponent> entityVariations = rawEntities.get(schema.entityName);
		SchemaComponent retSchema = schema;

		if (entityVariations != null)
		{
			Optional<SchemaComponent> foundSchema =
					entityVariations.stream().filter(schema::equals).findFirst();
			if (foundSchema.isPresent())
				retSchema = foundSchema.get();
			else
				entityVariations.add(schema);
		}
		else
		{
			List<SchemaComponent> ll = new ArrayList<SchemaComponent>(10);
			ll.add(schema);
			rawEntities.put(schema.entityName, ll);

			// Add the name of this entity to the list of afterward checking for already existing entities
			if (isRoot != ROOT_TYPE.ROOT_OBJECT)
				innerSchemaNames.add(schema.entityName);
		}

		return retSchema;
	}

	private SchemaComponent infer(IAJArray n, String elementName)
	{
		ArraySC schema = new ArraySC();

		// TODO: At this point we should use the ReferenceMatcher to test verbs such as has or Id.
		// If the name for this array can be made singular, do it.
		String singularName = Inflector.getInstance().singularize(elementName);

		final Optional<String> name = Optional.of(singularName);

		// We use a LinkedHashSet to merge similar elements
		// This way we simplify Aggr{V1, V2, V2, V2...V2} to Aggr{V1, V2}
		Iterable<IAJElement> iterable = () -> n.iterator();
		schema.addAll(StreamSupport.stream(iterable.spliterator(),false)
						.map(e -> infer(e, name, ROOT_TYPE.NON_ROOT_OBJECT, new ObjectMetadata()))
						.collect(Collectors.toCollection(LinkedHashSet::new)));

		return schema;
	}

	private SchemaComponent infer(IAJBoolean n, String elementName)
	{
		BooleanSC schema = new BooleanSC();
		return schema;
	}

	private SchemaComponent infer(IAJNumber n, String elementName)
	{
		NumberSC schema = new NumberSC();
		return schema;
	}

	private SchemaComponent infer(IAJNull n, String elementName)
	{
		NullSC schema = new NullSC();
		return schema;
	}

	private SchemaComponent infer(IAJTextual n, String elementName)
	{
		StringSC schema = new StringSC();
		return schema;
	}

	private SchemaComponent infer(IAJObjectId n, String elementName)
	{
		ObjectIdSC schema = new ObjectIdSC();
		return schema;
	}
}

package es.um.unosql.doc2unosql.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.inject.Inject;

import es.um.unosql.doc2unosql.intermediate.raw.ArraySC;
import es.um.unosql.doc2unosql.intermediate.raw.BooleanSC;
import es.um.unosql.doc2unosql.intermediate.raw.NullSC;
import es.um.unosql.doc2unosql.intermediate.raw.NumberSC;
import es.um.unosql.doc2unosql.intermediate.raw.ObjectIdSC;
import es.um.unosql.doc2unosql.intermediate.raw.ObjectSC;
import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;
import es.um.unosql.doc2unosql.intermediate.raw.StringSC;
import es.um.unosql.doc2unosql.process.util.FeatureAnalyzer;
import es.um.unosql.doc2unosql.process.util.MakePair;
import es.um.unosql.doc2unosql.process.util.OptionalTagger;
import es.um.unosql.doc2unosql.process.util.ReferenceMatcher;
import es.um.unosql.doc2unosql.process.util.ReferenceMatcherCreator;
import es.um.unosql.doc2unosql.process.util.StructuralVariationSorter;
import es.um.unosql.doc2unosql.util.inflector.Inflector;
import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.DataType;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.Key;
import es.um.unosql.uNoSQLSchema.PList;
import es.um.unosql.uNoSQLSchema.PTuple;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.StructuralFeature;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaFactory;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

public class UNoSQLModelBuilder
{
	private UNoSQLSchemaFactory factory;

	// Reverse indexes for finding StructuralVariations
	private Map<SchemaComponent, StructuralVariation> mStructuralVariations;
	private Map<String,List<Map.Entry<SchemaComponent, StructuralVariation>>> variationsPerEntity;

	// Reference matcher creator
	@Inject
	private ReferenceMatcherCreator<EntityType> rmCreator;

	// Reference matcher
	private ReferenceMatcher<EntityType> refMatcher;

	@Inject
	private StructuralVariationSorter varSorter;

	@Inject
	private OptionalTagger optTagger;

	@Inject
	private FeatureAnalyzer analyzer; 

	private uNoSQLSchema finalSchema;

	public UNoSQLModelBuilder()
	{
		_init();
	}

	public UNoSQLModelBuilder(ReferenceMatcherCreator<EntityType> rmCreator, StructuralVariationSorter varSorter, OptionalTagger optTagger, FeatureAnalyzer analyzer)
	{
		_init();
		this.rmCreator = rmCreator;
		this.varSorter = varSorter;
		this.optTagger = optTagger;
		this.analyzer = analyzer;
	}

	private void _init()
	{
		mStructuralVariations = new HashMap<>();
		variationsPerEntity = new HashMap<>();
	}

	public uNoSQLSchema build(UNoSQLSchemaFactory factory, String name, Map<String, List<SchemaComponent>> rawEntities)
	{
		this.factory = factory;

		finalSchema = factory.createuNoSQLSchema();
		finalSchema.setName(name);

		// TODO: Identify objects that are references in the form of MongoDB
		// references: https://docs.mongodb.org/manual/reference/database-references/#dbrefs
		// { "$ref" : <type>, "$id" : <value>, "$db" : <value> }

		// Build reverse indices & Create Entities & Populate with StructuralVariations
		rawEntities.forEach((entityTypeName, schemas) ->
		{
			EntityType entityType = factory.createEntityType();
			entityType.setName(entityTypeName);
			entityType.setRoot(schemas.stream().anyMatch(schema -> ((ObjectSC)schema).isRoot));

			finalSchema.getEntities().add(entityType);

			OfInt intIterator = IntStream.iterate(1, i -> i+1).iterator();

			List<Map.Entry<SchemaComponent,StructuralVariation>> variations =
					new ArrayList<>(20);
			schemas.forEach(schema ->
			{
				ObjectSC obj = (ObjectSC)schema;

				StructuralVariation theEV = factory.createStructuralVariation();
				theEV.setVariationId(intIterator.next());
				theEV.setCount(obj.meta.getCount());
				theEV.setFirstTimestamp(obj.meta.getFirstTimestamp());
				theEV.setLastTimestamp(obj.meta.getLastTimestamp());

				entityType.getVariations().add(theEV);
				mStructuralVariations.put(schema, theEV);
				variations.add(MakePair.of(schema, theEV));

				optTagger.put(entityTypeName, schema);
			});

			variationsPerEntity.put(entityTypeName,variations);
		});

		// TODO: Remove until recode
		// optTagger.calcOptionality();

		// Consider as reference matcher only those Entities of which at least one variation is root
		refMatcher = rmCreator.createReferenceMatcher(finalSchema.getEntities());

		// Populate empty StructuralVariations
		variationsPerEntity.forEach((n, le) ->
		le.forEach(e -> fillEV(n, e.getKey(),e.getValue())));

		// Finally try to sort each entity's StructuralVariations and set optionality
		finalSchema.getEntities().forEach(entity ->
		{
			varSorter.sort(entity.getVariations());
			analyzer.setOptionalProperties(entity.getVariations());
		});

		/** TODO: We are removing the opposite calculations for the moment since there is no easy way
		 * to infer these. On the near future we might try to complete this property but for the time being
		 * it is safer for the user to programatically find the opposites on demand.
		 */
		/*
      mEntities.forEach(eFrom -> {
        eFrom.getStructuralVariations().forEach(ev -> {
          ev.getProperties().stream().filter(p -> p instanceof Reference).forEach(r -> {
            Reference ref = (Reference)r;
            EntityType eTo = ref.getRefTo();

            // Find a StructuralVariation of eTo that has a reference to the
            // current EntityType eFrom
            Optional<Property> refTo =
              eTo.getStructuralVariations().stream().flatMap(evTo ->
              evTo.getProperties().stream().filter(pTo -> pTo instanceof Reference))
              .filter(rTo -> ((Reference)rTo).getRefTo() == eFrom).findFirst();

            refTo.ifPresent(r_ -> ref.setOpposite((Reference)r_));
          });
        });
      });
		 */
		return finalSchema;
	}

	private void fillEV(String evName, SchemaComponent schema, StructuralVariation ev)
	{
		assert(schema instanceof ObjectSC);

		// Set properties
		ev = ((ObjectSC)schema).getInners().stream().reduce(ev, (ev2, p) ->
		{
			StructuralFeature f = structuralFeatureFromSchemaComponent(p.getKey(), p.getValue());

			// Optionally, set optionality
			// TODO: Remove until recode
			// f.setOptional(optTagger.isOptional(evName, p));

			ev2.getFeatures().add(f);
			ev2.getStructuralFeatures().add(f);

			if (f instanceof Attribute)
			{
				maybeReference(Inflector.getInstance().singularize(p.getKey()), (Attribute)f)
				.ifPresent(ref ->
				{
					ev2.getFeatures().add(ref);
					ev2.getLogicalFeatures().add(ref);
				});

				if (f.getName().equals("_id"))
				{
					Key theKey = factory.createKey();
					((Attribute)f).setKey(theKey);
					ev2.getFeatures().add(theKey);
					ev2.getLogicalFeatures().add(theKey);
				}
			}

			return ev2;
		},
		(e,e2) -> e);
	}

	private StructuralFeature structuralFeatureFromSchemaComponent(String en, SchemaComponent sc)
	{
		if (sc instanceof ObjectSC)
			return structuralFeatureFromSchemaComponent(en, (ObjectSC)sc);

		if (sc instanceof ArraySC)
			return structuralFeatureFromSchemaComponent(en, (ArraySC)sc);

		if (sc instanceof BooleanSC || sc instanceof NumberSC || sc instanceof NullSC
				|| sc instanceof StringSC || sc instanceof ObjectIdSC)
			return structuralFeatureFromPrimitiveSchemaComponent(en, sc);

		return null;
	}

	private StructuralFeature structuralFeatureFromPrimitiveSchemaComponent(String en, SchemaComponent sc)
	{
		return structuralFeatureFromPrimitive(en, primitiveTypeFromSchemaComponent(sc));
	}

	private StructuralFeature structuralFeatureFromSchemaComponent(String en, ObjectSC sc)
	{
		// TODO: Check for complex DBRef references

		// Note that at this point, there is no need to recursively explore inner objects
		// as they have been all put at the root level in the previous phase.
		Aggregate a = factory.createAggregate();
		a.setName(en);
		a.setLowerBound(1);
		a.setUpperBound(1);
		a.getAggregates().add(mStructuralVariations.get(sc));
		return a;
	}

	private StructuralFeature structuralFeatureFromSchemaComponent(String en, ArraySC sc)
	{
		if (sc.isHomogeneous())
		{
			// If it is empty or it is NOT an Object (it is a simple type),
			// then it may be a reference
			SchemaComponent inner = sc.getInners().get(0);
			if (sc.size() == 0 || !(inner instanceof ObjectSC)) //TODO: Sospecho que no se entra nunca aqui. Ademas, si sc.size() == 0 entonces el inner de antes excepciona.
			{
				// Or else  build a PList with the correct types
				Attribute a = factory.createAttribute();
				a.setName(en);
				a.setType(PListOrPTupleForArray(sc));
				return a;
			}
			else
			{
				// size is not 0 and the homogeneous type is an object
				Aggregate a = factory.createAggregate();
				a.setName(en);
				// a.setLowerBound(sc.getLowerBounds() == 1 ? 0 : sc.getLowerBounds());
				// a.setUpperBound(sc.getUpperBounds() > 1 ? -1 : sc.getUpperBounds());
				a.setLowerBound(0);
				a.setUpperBound(-1);
				a.getAggregates().add(mStructuralVariations.get(inner));
				return a;
			}
		}

		// Non-homogeneous array. If all elements are objects, then
		// create an aggregate. If not, create a PList
		StructuralVariation ev = mStructuralVariations.get(sc.getInners().get(0));
		if (ev != null)
		{
			Aggregate a = factory.createAggregate();
			a.setName(en);
			a.setLowerBound(0);
			// a.setUpperBound(sc.getUpperBounds() > 1 ? -1 : sc.getUpperBounds());
			a.setUpperBound(-1);

			// FIXME: OJO, error en Ecore/EMF desde el 2005 sin arreglar, y aquí tiene problema:
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89325
			a.getAggregates().addAll(sc.getInners().stream()
					.map(mStructuralVariations::get)
					.collect(Collectors.toList()));

			return a;
		}
		else
		{
			// Or else  build a PList with the correct types
			Attribute a = factory.createAttribute();
			a.setName(en);
			a.setType(PListOrPTupleForArray(sc));
			return a;
		}
	}

	private DataType PListOrPTupleForArray(ArraySC sc)
	{
		// Return a list by default if empty array
		if (sc.size() == 0)
			return factory.createPList();

		if (sc.isHomogeneous())
		{
			PList t = factory.createPList();
			t.setElementType(recursiveTypeFromSchemaComponent(sc.getInners().get(0)));
			return t;
		}

		PTuple t = factory.createPTuple();

		t.getElements().addAll(
				sc.getInners().stream()
				.map(this::recursiveTypeFromSchemaComponent)
				.collect(Collectors.toList()));

		return t;
	}

	private DataType recursiveTypeFromSchemaComponent(SchemaComponent sc)
	{
		// Recursive
		if (sc instanceof ArraySC)
			return PListOrPTupleForArray((ArraySC)sc);

		// TODO: Consider Objects?
		String primType = primitiveTypeFromSchemaComponent(sc);
		PrimitiveType pt = factory.createPrimitiveType();
		pt.setName(primType);

		return pt;
	}

	private String primitiveTypeFromSchemaComponent(SchemaComponent sc)
	{
		if (sc instanceof BooleanSC)
			return "Boolean";

		if (sc instanceof NumberSC)
			return "Number";

		if (sc instanceof StringSC)
			return "String";

		if (sc instanceof ObjectIdSC)
			return "ObjectId";

		if (sc instanceof NullSC)
			return "Null";

		return "";
	}

	private Optional<Reference> maybeReference(String en, Attribute referenced)
	{
		return refMatcher.maybeMatch(en).map(e ->
		{
			Reference r = factory.createReference();
			r.setRefsTo(e);

			if (referenced.getType() instanceof PList)
			{
				r.setLowerBound(0);
				r.setUpperBound(-1);
			}
			else
			{
				r.setLowerBound(1);
				r.setUpperBound(1);
			}

			r.getAttributes().add(referenced);

			return r;
		});
	}

	private StructuralFeature structuralFeatureFromPrimitive(String en, String primitiveName)
	{
		Attribute a = factory.createAttribute();
		a.setName(en);
		PrimitiveType pt = factory.createPrimitiveType();
		pt.setName(primitiveName);
		a.setType(pt);
		return a;
	}
}

package es.um.unosql.doc2unosql.m2m;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.util.EcoreUtil;

import es.um.unosql.uNoSQLSchema.Aggregate;
import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.SchemaType;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaPackage;
import es.um.unosql.uNoSQLSchema.PMap;
import es.um.unosql.uNoSQLSchema.Reference;
import es.um.unosql.uNoSQLSchema.RelationshipType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.doc2unosql.util.inflector.Inflector;
import es.um.unosql.utils.ModelLoader;
import es.um.unosql.utils.UNoSQLFactory;
import es.um.unosql.utils.UNoSQLSchemaWriter;
import es.um.unosql.utils.compare.CompareStructuralVariation;

public class UNoSQLSchemaToDocumentDb
{
  private final static String REF_ENTITY_PREFIX = "Ref_";
  private final static String PMAP_ENTITY_PREFIX = "Map_";

  private UNoSQLFactory factory;

  public static void main(String[] args)
  {
    UNoSQLSchemaToDocumentDb var1 = new UNoSQLSchemaToDocumentDb();

    ModelLoader loader = new ModelLoader(UNoSQLSchemaPackage.eINSTANCE);
    uNoSQLSchema schema = loader.load(new File("json/dummy.xmi"), uNoSQLSchema.class);
    var1.adaptToDocumentDb(schema);
    UNoSQLSchemaWriter writer = new UNoSQLSchemaWriter();
    writer.write(schema, new File("json/dummy2.xmi"));
  }

  public UNoSQLSchemaToDocumentDb()
  {
    factory = new UNoSQLFactory();
  }

  public void adaptToDocumentDb(uNoSQLSchema schema)
  {
    List<RelationshipType> relTypes = new ArrayList<RelationshipType>();
    List<Attribute> mapAttributes = new ArrayList<Attribute>();

    for (SchemaType schemaT : Stream.concat(schema.getEntities().stream(), schema.getRelationships().stream()).collect(Collectors.toList()))
    {
      if (schemaT instanceof RelationshipType)
        relTypes.add((RelationshipType)schemaT);

      for (StructuralVariation var : schemaT.getVariations())
      {
        mapAttributes.addAll(var.getFeatures().stream().filter(prop ->
        prop instanceof Attribute && ((Attribute)prop).getType() instanceof PMap
            ).map(prop -> (Attribute)prop).collect(Collectors.toList()));
      }
    }

    relTypes.forEach(refClass -> relTypeToEntityType(schema, refClass));
    mapAttributes.forEach(attr -> removePMap(schema, attr));
  }

  /**
   * This method translates every RelationshipType on the current schema to an equivalent EntityType.
   * The new EntityType will contain every variation that the previous RelationshipType used to have,
   * so any Reference pointing to a RelationshipType variation will now point to an equivalent EntityType variation.
   * @param schema The schema being processed
   * @param relClass The RelationshipType being removed
   */
  private void relTypeToEntityType(uNoSQLSchema schema, RelationshipType relType)
  {
    CompareStructuralVariation comparator = new CompareStructuralVariation();
    List<Reference> lReferences = new ArrayList<Reference>();
    String entityName = REF_ENTITY_PREFIX + Inflector.getInstance().capitalize(relType.getName());

    // Get in a list each reference featuring a variation of the refClass
    for (SchemaType schemaType : Stream.concat(schema.getEntities().stream(), schema.getRelationships().stream()).collect(Collectors.toList()))
      for (StructuralVariation var : schemaType.getVariations())
        lReferences.addAll(var.getFeatures().stream()
            .filter(prop -> prop instanceof Reference && !Collections.disjoint(relType.getVariations(), ((Reference)prop).getIsFeaturedBy()))
            .map(prop -> (Reference)prop)
            .collect(Collectors.toList()));

    for (Reference ref : lReferences)
    {
      for (StructuralVariation var : ref.getIsFeaturedBy())
      {
        // We take the featured StructuralVariation and add a new Reference field.
        Reference newRef = factory.createReference(ref.getName(), ref.getLowerBound(), ref.getUpperBound(), ref.getRefsTo());
        newRef.setOpposite(ref.getOpposite());
        var.getFeatures().add(newRef); // What to do if it already exists a property named as the ref?
        var.getLogicalFeatures().add(newRef);

        for (Attribute attr : ref.getAttributes())
        {
          Attribute newAttr = EcoreUtil.copy(attr);
          var.getFeatures().add(newAttr);
          var.getStructuralFeatures().add(newAttr);
          newRef.getAttributes().add(attr);
        }

        if (!var.getFeatures().stream().anyMatch(prop -> prop.getName().equals("_id")))
          var.getFeatures().add(factory.createAttribute("_id", factory.createPrimitiveType("ObjectId")));
      }

      // We modify the current reference to a new cardinality
      ref.setLowerBound(1);
      ref.setUpperBound(1);
      ref.getIsFeaturedBy().clear();
    }

    // Create the new EntityType from a RelationshipType
    EntityType refEntity = null;
    Optional<EntityType> optEntity = schema.getEntities().stream().filter(entity -> entity.getName().equals(entityName)).findFirst();

    if (optEntity.isPresent())
      refEntity = optEntity.get();
    else
    {
      refEntity = factory.createEntityType(entityName);
      refEntity.setRoot(false);
      refEntity.getParents().addAll(relType.getParents());
      schema.getEntities().add(refEntity);
    }

    // We also modify the current references to reference the new EntityType
    for (Reference ref : lReferences)
      ref.setRefsTo(refEntity);

    // If an EntityType with the same name as a RelationshipType existed, we add the variations but take care of the variationId identifier.
    int varSize = refEntity.getVariations().size();
    if (varSize != 0)
    {
      List<StructuralVariation> varsToMove = new ArrayList<StructuralVariation>();
      for (StructuralVariation var : relType.getVariations())
        if (refEntity.getVariations().stream().noneMatch(innerVar -> comparator.compare(innerVar, var)))
        {
          var.setVariationId(++varSize);
          varsToMove.add(var);
        }
      refEntity.getVariations().addAll(varsToMove);
    }
    else
      refEntity.getVariations().addAll(relType.getVariations());

    schema.getRelationships().remove(relType);
  }

  /**
   * This method translated every PMap property to an equivalent Entity table with a variation with the same structure
   * as the PMap, and an aggregation. This is done because document-based NoSQL databases usually do not support Map structures,
   * but usually they support embedded documents. So for example, a property "pMapProp" of the type PMap<String, Int> will be
   * translated to an Entity called "Map_Pmapprop" with a variation with two properties (key: String, value: Int) and an aggregation
   * to that kind of variation, so the schema is unnafected. 
   * @param schema The schema being processed
   * @param attr The PMap attribute
   */
  private void removePMap(uNoSQLSchema schema, Attribute attr)
  {
    CompareStructuralVariation comparer = new CompareStructuralVariation();
    String entityName = PMAP_ENTITY_PREFIX + Inflector.getInstance().capitalize(attr.getName());
    PMap attrMap = (PMap)attr.getType();

    // First of all check if an entity with the same construction already exists. If not, just create it.
    EntityType mapEntity = null;
    Optional<EntityType> optEntity = schema.getEntities().stream().filter(entity -> {return entity.getName().equals(entityName);}).findFirst();

    if (optEntity.isPresent())
      mapEntity = optEntity.get();
    else
    {
      mapEntity = factory.createEntityType(entityName);
      mapEntity.setRoot(false);
      schema.getEntities().add(mapEntity);
    }

    // Now create a StructuralVariation with two properties: Key and value.
    Attribute key = factory.createAttribute("key", attrMap.getKeyType());
    Attribute value = factory.createAttribute("value", attrMap.getValueType());

    StructuralVariation compareVar = factory.createStructuralVariation(0);
    compareVar.getFeatures().add(key);
    compareVar.getFeatures().add(value);

    // Check if an equivalent StructuralVariation in the given Entity already exists.
    Optional<StructuralVariation> optVar = mapEntity.getVariations().stream().filter(var -> comparer.compare(compareVar, var)).findFirst();

    StructuralVariation theVar;
    if (optVar.isPresent())
      theVar = optVar.get();
    else
    {
      theVar = compareVar;
      int varId = mapEntity.getVariations().size() + 1;
      compareVar.setVariationId(varId);
      mapEntity.getVariations().add(compareVar);
    }

    // Exchange the PMap attribute with an aggregate.
    Aggregate aggr = factory.createAggregate(attr.getName(), 1, 1, theVar);
    aggr.setOptional(attr.isOptional());

    // The new aggregate will point to the StructuralVariation.
    // The old PMap attribute is unnecesary now.
    ((StructuralVariation)attr.eContainer()).getFeatures().add(aggr);
    ((StructuralVariation)attr.eContainer()).getFeatures().remove(attr);

    // Check this out! We do recursively remove every Map of a Map,
    // so cases like Map<String, Map<String, Int>> do not make us cry.
    if (value.getType() instanceof PMap)
      removePMap(schema, value);
  }
}

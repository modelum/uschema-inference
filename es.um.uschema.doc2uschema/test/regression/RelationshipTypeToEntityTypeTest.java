package regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.doc2uschema.m2m.USchemaToDocumentDb;
import es.um.uschema.utils.USchemaSerializer;
import es.um.uschema.USchema.USchemaFactory;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.RelationshipType;
import es.um.uschema.USchema.StructuralVariation;

public class RelationshipTypeToEntityTypeTest
{
  private USchemaFactory factory;
  private USchemaToDocumentDb schema2DDb;
  private USchemaSerializer serializer;

  @Before
  public void setUp()
  {
    factory = USchemaFactory.eINSTANCE;
    schema2DDb = new USchemaToDocumentDb();
    serializer = new USchemaSerializer();
  }

  @Test
  public void testRelationshipType()
  {
    RelationshipType refClass = createRelType("class1", 3);
    USchema schema = factory.createUSchema();
    schema.setName("schema");
    schema.getRelationships().add(refClass);

    schema2DDb.adaptToDocumentDb(schema);

    assertTrue(schema.getRelationships().isEmpty());
    assertEquals(1, schema.getEntities().size());
    assertEquals(3, schema.getEntities().get(0).getVariations().size());
  }

  @Test
  public void testRelationshipTypeCollision()
  {
    RelationshipType refClass = createRelType("class1", 15);
    EntityType entityType = createEntityType("Ref_Class1", 4);

    USchema schema = factory.createUSchema();
    schema.setName("schema");
    schema.getRelationships().add(refClass);
    schema.getEntities().add(entityType);

    schema2DDb.adaptToDocumentDb(schema);

    assertTrue(schema.getRelationships().isEmpty());
    assertEquals(1, schema.getEntities().size());
    assertEquals(15, schema.getEntities().get(0).getVariations().size());
  }

  @Test
  public void testFixReferences()
  {
    RelationshipType refClass = createRelType("relClass", 1);
    EntityType entity1 = createEntityType("entity1", 1);
    EntityType entity2 = createEntityType("entity2", 1);

    USchema schema = factory.createUSchema();
    schema.setName("schema");
    schema.getRelationships().add(refClass);
    schema.getEntities().add(entity1); schema.getEntities().add(entity2);

    Reference ref = factory.createReference();
    ref.setName("theReference");
    ref.setLowerBound(1);
    ref.setUpperBound(2);
    ref.setRefsTo(entity2);
    ref.getIsFeaturedBy().add(refClass.getVariations().get(0));
    entity1.getVariations().get(0).getFeatures().add(ref);

    schema2DDb.adaptToDocumentDb(schema);

    assertTrue(schema.getRelationships().isEmpty());
    assertEquals(3, schema.getEntities().size());
    assertTrue(schema.getEntities().get(0).getVariations().get(0).getFeatures().stream().noneMatch(prop -> {return prop instanceof Aggregate;}));
  }

  @Test
  public void testFixReferencesAndCollision()
  {
    RelationshipType refClass = createRelType("relClass", 1);
    EntityType entity1 = createEntityType("entity1", 1);
    EntityType entity2 = createEntityType("entity2", 1);
    EntityType entity3 = createEntityType("Ref_Relclass", 3);

    USchema schema = factory.createUSchema();
    schema.setName("schema");
    schema.getRelationships().add(refClass);
    schema.getEntities().add(entity1); schema.getEntities().add(entity2); schema.getEntities().add(entity3);

    Reference ref = factory.createReference();
    ref.setName("theReference");
    ref.setLowerBound(1);
    ref.setUpperBound(2);
    ref.setRefsTo(entity2);
    ref.getIsFeaturedBy().add(refClass.getVariations().get(0));
    entity1.getVariations().get(0).getFeatures().add(ref);

    System.out.println(serializer.serialize(schema));
    schema2DDb.adaptToDocumentDb(schema);
    System.out.println(serializer.serialize(schema));
  }

  private RelationshipType createRelType(String name, int variations)
  {
    RelationshipType refClass = factory.createRelationshipType();
    refClass.setName(name);

    for (int i = 1; i <= variations; i++)
    {
      StructuralVariation var = factory.createStructuralVariation();
      var.setVariationId(i);
      refClass.getVariations().add(var);

      PrimitiveType pType = factory.createPrimitiveType(); pType.setName("string");
      Attribute attr = factory.createAttribute(); attr.setName("attr" + i); attr.setType(pType);
      var.getFeatures().add(attr);
    }

    return refClass;
  }

  private EntityType createEntityType(String name, int variations)
  {
    EntityType entity = factory.createEntityType();
    entity.setName(name);

    for (int i = 1; i <= variations; i++)
    {
      StructuralVariation var = factory.createStructuralVariation();
      var.setVariationId(i);
      entity.getVariations().add(var);

      PrimitiveType pType = factory.createPrimitiveType(); pType.setName("string");
      Attribute attr = factory.createAttribute(); attr.setName("attr" + i); attr.setType(pType);
      var.getFeatures().add(attr);
    }

    return entity;
  }
}

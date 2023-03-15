package regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.USchemaFactory;
import es.um.uschema.doc2uschema.m2m.USchemaToDocumentDb;
import es.um.uschema.USchema.PMap;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.StructuralVariation;

public class RemovePMapTest
{
  private USchemaFactory factory;
  private USchemaToDocumentDb schema2DDb;

  @Before
  public void setUp()
  {
    factory = new USchemaFactory();
    schema2DDb = new USchemaToDocumentDb();
  }

  @Test
  public void testRemovePMap()
  {
    Attribute attrMap = factory.createAttribute("attr2map", factory.createPMap(
        factory.createPrimitiveType("string"),
        factory.createPrimitiveType("string")));

    StructuralVariation var = factory.createStructuralVariation(1);
    var.getFeatures().add(factory.createAttribute("attr1Str", factory.createPrimitiveType("string")));
    var.getFeatures().add(attrMap);

    EntityType entity = factory.createEntityType("entityName");
    entity.setName("entityName");
    entity.getVariations().add(var);

    USchema schema = factory.createUSchema("testRemovePMap");
    schema.getEntities().add(entity);

    schema2DDb.adaptToDocumentDb(schema);

    assertEquals(2, schema.getEntities().size());
    assertEquals("entityName", schema.getEntities().get(0).getName());
    assertEquals(1, schema.getEntities().get(0).getVariations().size());
    assertEquals(2, schema.getEntities().get(0).getVariations().get(0).getFeatures().size());
    Feature feat1 = schema.getEntities().get(0).getVariations().get(0).getFeatures().get(0);
    Feature feat2 = schema.getEntities().get(0).getVariations().get(0).getFeatures().get(1);
    assertTrue(feat1 instanceof Attribute && feat1.getName().equals("attr1Str"));
    assertTrue(feat2 instanceof Aggregate && feat2.getName().equals("attr2map"));

    assertEquals("Map_Attr2map", schema.getEntities().get(1).getName());
    assertEquals(1, schema.getEntities().get(1).getVariations().size());
    assertEquals(2, schema.getEntities().get(1).getVariations().get(0).getFeatures().size());
    feat1 = schema.getEntities().get(1).getVariations().get(0).getFeatures().get(0);
    feat2 = schema.getEntities().get(1).getVariations().get(0).getFeatures().get(1);
    assertTrue(feat1 instanceof Attribute && feat1.getName().equals("key"));
    assertTrue(feat2 instanceof Attribute && feat2.getName().equals("value"));
  }

  @Test
  public void testRemovePMapFromTwoVariations()
  {
    Attribute attrMap = factory.createAttribute("attr2map", factory.createPMap(
        factory.createPrimitiveType("string"),
        factory.createPrimitiveType("string")));
    Attribute attrMap2 = factory.createAttribute("attr2map", factory.createPMap(
        factory.createPrimitiveType("string"),
        factory.createPrimitiveType("string")));

    StructuralVariation var = factory.createStructuralVariation(1);
    var.getFeatures().add(factory.createAttribute("attr1Str", factory.createPrimitiveType("string")));
    var.getFeatures().add(attrMap);

    StructuralVariation var2 = factory.createStructuralVariation(2);
    var2.getFeatures().add(attrMap2);

    EntityType entity = factory.createEntityType("entityName");
    entity.getVariations().add(var);
    entity.getVariations().add(var2);

    USchema schema = factory.createUSchema("testRemovePMapFromTwoVariations");
    schema.getEntities().add(entity);

    schema2DDb.adaptToDocumentDb(schema);

    assertEquals(2, schema.getEntities().size());
    assertEquals("entityName", schema.getEntities().get(0).getName());
    assertEquals(2, schema.getEntities().get(0).getVariations().size());
    assertEquals(2, schema.getEntities().get(0).getVariations().get(0).getFeatures().size());
    assertEquals(1, schema.getEntities().get(0).getVariations().get(1).getFeatures().size());
    Feature feat1 = schema.getEntities().get(0).getVariations().get(0).getFeatures().get(0);
    Feature feat2 = schema.getEntities().get(0).getVariations().get(0).getFeatures().get(1);
    assertTrue(feat1 instanceof Attribute && feat1.getName().equals("attr1Str"));
    assertTrue(feat2 instanceof Aggregate && feat2.getName().equals("attr2map"));
    feat1 = schema.getEntities().get(0).getVariations().get(1).getFeatures().get(0);
    assertTrue(feat2 instanceof Aggregate && feat2.getName().equals("attr2map"));

    assertEquals("Map_Attr2map", schema.getEntities().get(1).getName());
    assertEquals(1, schema.getEntities().get(1).getVariations().size());
    assertEquals(2, schema.getEntities().get(1).getVariations().get(0).getFeatures().size());
    feat1 = schema.getEntities().get(1).getVariations().get(0).getFeatures().get(0);
    feat2 = schema.getEntities().get(1).getVariations().get(0).getFeatures().get(1);
    assertTrue(feat1 instanceof Attribute && feat1.getName().equals("key"));
    assertTrue(feat2 instanceof Attribute && feat2.getName().equals("value"));
  }

  @Test
  public void testRecursiveRemovePMap()
  {
    PMap pTypeMap = factory.createPMap(factory.createPrimitiveType("int"), factory.createPrimitiveType("int"));
    Attribute attrMap = factory.createAttribute("attr2map", factory.createPMap(factory.createPrimitiveType("string"), pTypeMap));

    Attribute attrMap2 = factory.createAttribute("attr2map", factory.createPMap(factory.createPrimitiveType("string"), factory.createPrimitiveType("int")));

    StructuralVariation var1 = factory.createStructuralVariation(1);
    var1.getFeatures().add(factory.createAttribute("attr1Str", factory.createPrimitiveType("string")));
    var1.getFeatures().add(attrMap);

    StructuralVariation var2 = factory.createStructuralVariation(2);
    var2.getFeatures().add(attrMap2);

    EntityType entity = factory.createEntityType("entityName");
    entity.getVariations().add(var1); entity.getVariations().add(var2);

    USchema schema = factory.createUSchema("testRecursiveRemovePMap");
    schema.getEntities().add(entity);

    schema2DDb.adaptToDocumentDb(schema);
    assertTrue(var1.getFeatures().stream().noneMatch(prop -> {return prop instanceof Attribute && ((Attribute)prop).getType() instanceof PMap;}));
    assertTrue(var2.getFeatures().stream().noneMatch(prop -> {return prop instanceof Attribute && ((Attribute)prop).getType() instanceof PMap;}));
  }
}

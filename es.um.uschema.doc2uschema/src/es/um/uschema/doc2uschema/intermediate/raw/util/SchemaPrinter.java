package es.um.uschema.doc2uschema.intermediate.raw.util;

import java.util.List;
import java.util.Map;

import es.um.uschema.doc2uschema.intermediate.raw.ArraySC;
import es.um.uschema.doc2uschema.intermediate.raw.BooleanSC;
import es.um.uschema.doc2uschema.intermediate.raw.NullSC;
import es.um.uschema.doc2uschema.intermediate.raw.NumberSC;
import es.um.uschema.doc2uschema.intermediate.raw.ObjectIdSC;
import es.um.uschema.doc2uschema.intermediate.raw.ObjectSC;
import es.um.uschema.doc2uschema.intermediate.raw.SchemaComponent;
import es.um.uschema.doc2uschema.intermediate.raw.StringSC;

public class SchemaPrinter
{
  public static void schemaEntities(Map<String, List<SchemaComponent>> rawEntities)
  {
    rawEntities.forEach((entity, evList) ->
    {
      System.out.println("Entity: " + entity);

      evList.forEach(ev ->
      {
        System.out.print("* ");
        System.out.println(schemaString(ev));
      });
    });
  }

  public static String schemaString(SchemaComponent sc)
  {
    StringBuilder sb = new StringBuilder();
    return schemaString(sc, sb);
  }

  private static String schemaString(SchemaComponent sc, StringBuilder sb)
  {
    if (sc instanceof ObjectSC)
      schemaString((ObjectSC)sc, sb);

    if (sc instanceof ArraySC)
      schemaString((ArraySC)sc, sb);

    if (sc instanceof BooleanSC)
      sb.append("Bool");

    if (sc instanceof NumberSC)
      sb.append("Number");

    if (sc instanceof NullSC)
      sb.append("Null");

    if (sc instanceof ObjectIdSC)
      sb.append("Oid");

    if (sc instanceof StringSC)
      sb.append("String");

    sb.append(' ');

    return sb.toString();
  }

  private static void _outname(String name, StringBuilder sb)
  {
    sb.append("\"" + name + "\": ");
  }

  private static void schemaString(ObjectSC sc, StringBuilder sb)
  {
    if (sc.isRoot)
      sb.append("(root)");
    if (sc.meta != null)
    {
      sb.append("(count: " + sc.meta.getCount() + ")");
      sb.append("(firstTs: " + sc.meta.getFirstTimestamp() + ")");
      sb.append("(lastTs: " + sc.meta.getLastTimestamp() + ")");      
    }
    sb.append("<" + sc.entityName + ">{");
    sc.getInners().forEach(e -> {
      _outname(e.getKey(), sb);
      schemaString(e.getValue(), sb);
    });
    sb.append("}");
  }

  private static void schemaString(ArraySC sc, StringBuilder sb)
  {
    sb.append("[");
    sc.getInners().forEach(e -> schemaString(e, sb));
    sb.append("]");
  }
}

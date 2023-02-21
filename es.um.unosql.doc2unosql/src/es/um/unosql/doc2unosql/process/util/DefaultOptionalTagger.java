package es.um.unosql.doc2unosql.process.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.um.unosql.doc2unosql.intermediate.raw.ObjectSC;
import es.um.unosql.doc2unosql.intermediate.raw.SchemaComponent;

public class DefaultOptionalTagger implements OptionalTagger
{
  private Map<String, List<SchemaComponent>> mSVByEntity;
  private Map<String, Map<Map.Entry<String,SchemaComponent>, Integer>> optionalsByEntity;

  public DefaultOptionalTagger()
  {
    mSVByEntity = new HashMap<>();
    optionalsByEntity = new HashMap<>();
  }

  @Override
  public void put(String entityTypeName, SchemaComponent schema)
  {
    List<SchemaComponent> l;
    if ((l = mSVByEntity.get(entityTypeName)) == null)
    {
      l = new ArrayList<>(20);
      mSVByEntity.put(entityTypeName, l);
    }
    l.add(schema);
  }

  @Override
  public void calcOptionality()
  {
    mSVByEntity.entrySet().stream().forEach(e ->
    {
      Map<Map.Entry<String,SchemaComponent>,Integer> featCount = new HashMap<>();
      optionalsByEntity.put(e.getKey(), featCount);

      int numVariations = e.getValue().size();

      // Optimization for just one variation
      if (numVariations == 1)
        return;

      e.getValue().stream().flatMap(sc ->
        ((ObjectSC)sc).getInners().stream())
        .reduce(featCount,
          (fc, sc_) -> {
            int val = fc.getOrDefault(sc_, 0);
            fc.put(sc_, val+1);
            return fc;
          },
          (e2,e3) -> e2);

      featCount.entrySet().removeIf(entry -> entry.getValue() == numVariations);
    });
  }

  @Override
  public boolean isOptional(String entityName, Map.Entry<String,SchemaComponent> sc)
  {
    return optionalsByEntity.get(entityName).containsKey(sc);
  }
}

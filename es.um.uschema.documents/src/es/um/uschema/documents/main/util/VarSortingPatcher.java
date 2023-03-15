package es.um.uschema.documents.main.util;

import java.nio.file.Path;

import org.eclipse.emf.common.util.ECollections;

import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.USchema.USchema;

public class VarSortingPatcher
{
  public static void main(String[] args)
  {
    VarSortingPatcher patcher = new VarSortingPatcher();
    patcher.patch("../es.um.uschema.models/stackoverflow/stackoverflow.xmi");
  }

  public void patch(String modelRoute)
  {
    EcoreModelIO modelIO = new EcoreModelIO();
    USchema schema = modelIO.load(USchema.class, Path.of(modelRoute));

    schema.getEntities().forEach(entity ->
    {
      ECollections.sort(entity.getVariations(), (var1, var2) -> var1.getFirstTimestamp() > var2.getFirstTimestamp() ? 1 : -1);

      for (int i = 1; i <= entity.getVariations().size(); i++)
        entity.getVariations().get(i - 1).setVariationId(i);
    });

    modelIO.write(schema, Path.of(modelRoute));
  }
}

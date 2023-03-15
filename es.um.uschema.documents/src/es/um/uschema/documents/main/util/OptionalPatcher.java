package es.um.uschema.documents.main.util;

import java.nio.file.Path;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.StructuralFeature;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.doc2uschema.process.util.DefaultFeatureAnalyzer;
import es.um.uschema.doc2uschema.process.util.FeatureAnalyzer;

public class OptionalPatcher {

  public static void main(String[] args)
  {
    OptionalPatcher patcher = new OptionalPatcher();
    patcher.patch("../es.um.uschema.models/reddit/reddit.xmi");
  }

  public void patch(String modelRoute)
  {
    EcoreModelIO modelIO = new EcoreModelIO();
    USchema schema = modelIO.load(USchema.class, Path.of(modelRoute));

    FeatureAnalyzer analyzer = new DefaultFeatureAnalyzer();

    for (EntityType e : schema.getEntities())
    {
      e.getVariations().forEach(var -> { var.getFeatures().forEach(feat -> { if (feat instanceof StructuralFeature) ((Attribute)feat).setOptional(false); }); });
      analyzer.setOptionalProperties(e.getVariations());
    }

    modelIO.write(schema, Path.of(modelRoute));
  }
}

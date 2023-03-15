package es.um.uschema.doc2uschema.process.util;

import java.util.List;

import es.um.uschema.USchema.StructuralVariation;

public interface FeatureAnalyzer
{
  void setOptionalProperties(List<StructuralVariation> variations);
}

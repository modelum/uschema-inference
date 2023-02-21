package es.um.unosql.doc2unosql.process.util;

import java.util.List;

import es.um.unosql.uNoSQLSchema.StructuralVariation;

public interface FeatureAnalyzer
{
  void setOptionalProperties(List<StructuralVariation> variations);
}

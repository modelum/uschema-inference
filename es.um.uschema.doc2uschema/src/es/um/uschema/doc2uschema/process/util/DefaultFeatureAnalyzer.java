package es.um.uschema.doc2uschema.process.util;

import java.util.ArrayList;
import java.util.List;

import es.um.uschema.USchema.StructuralFeature;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.utils.compare.CompareFeature;

public class DefaultFeatureAnalyzer implements FeatureAnalyzer
{
  // This comparer will tell us if two properties are the same based on the formal Feature comparator
  private CompareFeature comparer;

  public DefaultFeatureAnalyzer()
  {
    comparer = new CompareFeature();
  }

  @Override
  public void setOptionalProperties(List<StructuralVariation> variations)
  {
    // At most each Feature of the first variation will be a common Feature.
    List<StructuralFeature> commonProps = new ArrayList<StructuralFeature>();
    commonProps.addAll(variations.get(0).getStructuralFeatures());

    List<StructuralFeature> optionalProps = new ArrayList<StructuralFeature>();

    // This loop will reduce the list of common properties checking if each Feature is actually common to all variations
    for (StructuralFeature prop : commonProps)
      if (!variations.stream().allMatch(var -> var == variations.get(0) || var.getStructuralFeatures().stream().anyMatch(sf -> comparer.compare(prop, sf))))
        optionalProps.add(prop);

    // Now for sure we have all common properties on this list.
    commonProps.removeAll(optionalProps);

    // For each Feature, if it is not on the common properties list, flag it as optional.
    for (StructuralVariation var: variations)
      var.getStructuralFeatures().forEach(sf -> sf.setOptional(commonProps.stream().noneMatch(cProp -> comparer.compare(sf, cProp))));
  }
}

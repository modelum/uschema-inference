package es.um.uschema.doc2uschema.process.util;

import org.eclipse.emf.common.util.EList;

import es.um.uschema.USchema.StructuralVariation;

public interface StructuralVariationSorter
{
	void sort(EList<StructuralVariation> vars);
}
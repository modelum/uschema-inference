package es.um.unosql.doc2unosql.process.util;

import org.eclipse.emf.common.util.EList;

import es.um.unosql.uNoSQLSchema.StructuralVariation;

public interface StructuralVariationSorter
{
	void sort(EList<StructuralVariation> vars);
}
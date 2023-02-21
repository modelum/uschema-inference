package es.um.unosql.doc2unosql.process.util;

import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;

import es.um.unosql.uNoSQLSchema.StructuralVariation;

public class DefaultStructuralVariationSorter implements StructuralVariationSorter
{
	@Override
	public void sort(EList<StructuralVariation> vars)
	{
		if (vars.stream().anyMatch(var -> var.getFirstTimestamp() != 0))
			sortByFirstTimestamp(vars);
		else if (vars.stream().anyMatch(var -> var.getLastTimestamp() != 0))
			sortByLastTimestamp(vars);
		else if (vars.stream().anyMatch(var -> var.getCount() != 0))
			sortByCount(vars);
		else
			sortByPropertyNumber(vars);
	}

	private void sortByFirstTimestamp(EList<StructuralVariation> vars)
	{
		ECollections.sort(vars, (var1, var2) -> var1.getFirstTimestamp() < var2.getFirstTimestamp() ? -1 : 1);
		reorderVariationIds(vars);
	}

	private void sortByLastTimestamp(EList<StructuralVariation> vars)
	{
		ECollections.sort(vars, (var1, var2) -> var1.getLastTimestamp() < var2.getLastTimestamp() ? -1 : 1);
		reorderVariationIds(vars);
	}

	private void sortByCount(EList<StructuralVariation> vars)
	{
		//ECollections.sort(vars, (var1, var2) -> var1.getCount() > var2.getCount() ? -1 : 1);
		reorderVariationIds(vars);
	}

	private void sortByPropertyNumber(EList<StructuralVariation> vars)
	{
		ECollections.sort(vars, (var1, var2) -> var1.getFeatures().size() < var2.getFeatures().size() ? -1 : 1);
		reorderVariationIds(vars);
	}

	private void reorderVariationIds(EList<StructuralVariation> vars)
	{
		OfInt it = IntStream.range(1, vars.size()+1).iterator();
		vars.stream().forEach(sv -> sv.setVariationId(it.next()));
	}
}

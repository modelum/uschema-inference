package es.um.unosql.redis2unosql.spark.map;

import static es.um.unosql.redis2unosql.constants.Constants.COLON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class IdKeyMapping implements PairFunction<String, Integer, Collection<String>>
{
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<Integer, Collection<String>> call(String k) throws Exception 
	{
		int lioc = k.lastIndexOf(COLON);
		// User:123:address.city
		int key = k.substring(0, lioc).hashCode();
		// address.city
		String value = k.substring(0, k.indexOf(COLON)) + ":*:" + k.substring(lioc + 1);
		
		int liofdot;
		if ((liofdot = value.lastIndexOf('.')) != -1)
		{
			if (value.charAt(liofdot - 2) == '.')
				value = value.substring(0, liofdot - 1) + "*" + value.substring(liofdot);
		}
		
		List<String> atts = new ArrayList<String>(1);
		atts.add(value);
		
		return new Tuple2<Integer, Collection<String>>(key, atts);
	}
}

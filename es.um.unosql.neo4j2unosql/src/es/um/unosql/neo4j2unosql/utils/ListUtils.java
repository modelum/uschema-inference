package es.um.unosql.neo4j2unosql.utils;

import java.util.Iterator;
import java.util.LinkedList;

public class ListUtils
{
	public static <A> Iterator<A> emptyList() {
		return new LinkedList<A>().iterator();
	}

	public static Iterator<String> toList(String... strings)
	{
		return new Iterator<String>()
		{
			private int i = 0;
			
			@Override
			public boolean hasNext() 
			{
				return i < strings.length;
			}

			@Override
			public String next() 
			{
				return strings[i++];
			}
		};
	}

	public static Iterator<String> toList(String string, String[] strings)
	{
		return new Iterator<String>()
		{
			private int i = 0;
			
			@Override
			public boolean hasNext() 
			{
				return i < strings.length + 1;
			}

			@Override
			public String next() 
			{
				++i;
				if (i == 1)
					return string;

				return strings[i-2];
			}
		};
	}
}

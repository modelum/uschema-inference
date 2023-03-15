package es.um.uschema.neo4j2uschema.utils;

import java.util.HashMap;
import java.util.Map;

public class MapCounter <T>
{
	private Map<T, Integer> mapCounter;
	
	public MapCounter()
	{
		this.mapCounter = new HashMap<T, Integer>();
	}
	
	public void put(T item)
	{
		Integer integer = mapCounter.get(item);
		if (integer == null) 
		{
			integer = 0;
		}
		
		integer++;
		mapCounter.put(item, integer);
	}
	
	public int get(T item)
	{
		return mapCounter.get(item);
	}
	
}

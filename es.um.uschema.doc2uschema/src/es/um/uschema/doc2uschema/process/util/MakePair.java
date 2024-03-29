/**
 * 
 */
package es.um.uschema.doc2uschema.process.util;

import java.util.AbstractMap;
import java.util.Map;

/**
 * @author dsevilla
 *
 */
public class MakePair 
{
	public static <K,V> Map.Entry<K, V> of(K key, V value)
	{
		return new AbstractMap.SimpleEntry<K,V>(key,value);
	}
}

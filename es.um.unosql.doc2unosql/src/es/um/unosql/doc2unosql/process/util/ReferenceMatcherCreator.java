/**
 * 
 */
package es.um.unosql.doc2unosql.process.util;

import java.util.List;

/**
 * @author dsevilla
 *
 */
public interface ReferenceMatcherCreator<T> 
{
	ReferenceMatcher<T> createReferenceMatcher(List<T> elements);
}

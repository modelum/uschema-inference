/**
 * 
 */
package es.um.uschema.doc2uschema.process.util;

import java.util.List;

/**
 * @author dsevilla
 *
 */
public interface ReferenceMatcherCreator<T> 
{
	ReferenceMatcher<T> createReferenceMatcher(List<T> elements);
}

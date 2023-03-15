/**
 *
 */
package es.um.uschema.doc2uschema.intermediate.firsto;

import java.util.Set;

/**
 * @author dsevilla
 *
 */
public interface MultiValued
{
	public abstract Set<? extends Comparable<?>> getValues();
}

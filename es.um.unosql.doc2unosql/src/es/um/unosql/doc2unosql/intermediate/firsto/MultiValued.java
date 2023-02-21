/**
 *
 */
package es.um.unosql.doc2unosql.intermediate.firsto;

import java.util.Set;

/**
 * @author dsevilla
 *
 */
public interface MultiValued
{
	public abstract Set<? extends Comparable<?>> getValues();
}

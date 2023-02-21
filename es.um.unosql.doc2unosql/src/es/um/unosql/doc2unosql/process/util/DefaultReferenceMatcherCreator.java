/**
 *
 */
package es.um.unosql.doc2unosql.process.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import es.um.unosql.doc2unosql.util.inflector.Inflector;
import es.um.unosql.uNoSQLSchema.EntityType;

/**
 * @author dsevilla
 *
 */
public class DefaultReferenceMatcherCreator implements ReferenceMatcherCreator<EntityType>
{
	@Override
	public ReferenceMatcher<EntityType> createReferenceMatcher(List<EntityType> elements) {
		return new DefaultReferenceMatcher<>(elements.stream()
				.filter(EntityType::isRoot)
				.map(e ->
				MakePair.of(new HashSet<String>(Arrays.asList(
								e.getName(),
								Inflector.getInstance().pluralize(e.getName()),
								Inflector.getInstance().singularize(e.getName())))
								,e))
				.flatMap(p -> p.getKey().stream().map(s -> MakePair.of(s,p.getValue()))));
	}

}

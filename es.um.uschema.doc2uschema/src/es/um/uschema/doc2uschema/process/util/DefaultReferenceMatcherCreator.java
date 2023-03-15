/**
 *
 */
package es.um.uschema.doc2uschema.process.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.doc2uschema.util.inflector.Inflector;

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

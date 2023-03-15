package es.um.uschema.doc2uschema.process.util;

import java.util.Optional;

public interface ReferenceMatcher<T>
{
	Optional<T> maybeMatch(String id);
}
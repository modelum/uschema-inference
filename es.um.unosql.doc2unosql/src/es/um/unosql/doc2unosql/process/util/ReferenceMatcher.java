package es.um.unosql.doc2unosql.process.util;

import java.util.Optional;

public interface ReferenceMatcher<T>
{
	Optional<T> maybeMatch(String id);
}
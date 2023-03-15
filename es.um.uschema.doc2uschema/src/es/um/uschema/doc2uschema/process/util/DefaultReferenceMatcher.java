/**
 * 
 */
package es.um.uschema.doc2uschema.process.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dsevilla
 *
 */
public class DefaultReferenceMatcher<T> implements ReferenceMatcher<T>
{
	// List of affixes to check for references
	private static List<String> Affixes =
			Arrays.asList("id", "ptr", "ref", "ids", "refs", "has", "");

	private static List<String> StopChars =
			Arrays.asList("_", ".", "-", "");

	// Unlikely words to appear in a reference
	private static List<String> UnlikelyWords = Arrays.asList("count");

	// TODO: By using a list this matcher is just too slow.
	// A different approach might be to test each key with an affix and a suffix,
	// or let the user solve the references, or use a configuration file.
	// For now we will keep this as it is.
	private List<Map.Entry<String, T>> idRegexps;

	public DefaultReferenceMatcher(Stream<Map.Entry<String, T>> stream)
	{
		// Build the regexp that will allow checking if a field may be a reference to another entity
		idRegexps = stream.flatMap(entry ->
			Affixes.stream().flatMap(affix ->
				Stream.concat(
					// prefix
					StopChars.stream().map(c ->
						MakePair.of(("^" + entry.getKey() + c + affix + ".*$").toLowerCase(), entry.getValue())),
					Stream.concat(StopChars.stream().map(c ->
							MakePair.of(("^" + affix + c + entry.getKey() + ".*$").toLowerCase(), entry.getValue())),
						// postfix
						Stream.concat(StopChars.stream().filter(c -> !c.isEmpty() || !affix.isEmpty()).map(c ->
								MakePair.of(("^.*?" + entry.getKey() + c + affix + "$").toLowerCase(), entry.getValue())),								
									StopChars.stream().filter(c -> !c.isEmpty() || !affix.isEmpty()).map(c ->
										MakePair.of(("^.*?" + affix + c + entry.getKey() + "$").toLowerCase(), entry.getValue()))))
				)
			)
		).collect(Collectors.toList());
	}

	@Override
	public Optional<T> maybeMatch(String id)
	{
		if (UnlikelyWords.stream().anyMatch(w -> id.toLowerCase().contains(w)))
			return Optional.<T>empty();

		return idRegexps.stream().filter(pair -> id.toLowerCase().matches(pair.getKey())).findFirst().map(Map.Entry::getValue);
	}
}

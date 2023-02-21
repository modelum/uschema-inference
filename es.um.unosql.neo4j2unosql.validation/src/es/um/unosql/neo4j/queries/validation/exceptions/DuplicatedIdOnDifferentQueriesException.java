package es.um.unosql.neo4j.queries.validation.exceptions;

public class DuplicatedIdOnDifferentQueriesException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DuplicatedIdOnDifferentQueriesException()
	{
		super();
	}

	public DuplicatedIdOnDifferentQueriesException(String message)
	{
		super(message);
	}
	
}

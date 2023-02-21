package es.um.unosql.hbase2unosql.validation.queries.exceptions;

public class DuplicatedIdException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DuplicatedIdException() {
		super();
	}

	public DuplicatedIdException(String message) {
		super(message);
	}
	
}

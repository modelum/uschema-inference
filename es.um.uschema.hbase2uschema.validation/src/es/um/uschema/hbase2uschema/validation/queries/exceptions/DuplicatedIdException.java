package es.um.uschema.hbase2uschema.validation.queries.exceptions;

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

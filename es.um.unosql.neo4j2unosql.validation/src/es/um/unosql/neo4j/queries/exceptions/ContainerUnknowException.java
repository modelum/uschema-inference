package es.um.unosql.neo4j.queries.exceptions;

public class ContainerUnknowException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ContainerUnknowException()
	{
		super();
	}

	public ContainerUnknowException(String msg) {
		super(msg);
	}
	
}

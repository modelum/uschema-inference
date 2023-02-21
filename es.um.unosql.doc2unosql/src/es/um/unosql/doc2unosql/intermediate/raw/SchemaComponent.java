package es.um.unosql.doc2unosql.intermediate.raw;

public abstract class SchemaComponent
{
	@Override
	public boolean equals(Object other)
	{
		return getClass().getName().equals(other.getClass().getName());
	}

	@Override
	public int hashCode()
	{
		return getClass().getName().hashCode();
	}
}

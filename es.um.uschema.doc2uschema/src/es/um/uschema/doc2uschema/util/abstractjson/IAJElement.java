package es.um.uschema.doc2uschema.util.abstractjson;

import java.util.Iterator;

public interface IAJElement extends Iterable<IAJElement>, IAJIdentify
{
	public IAJElement get(int index);

	public IAJElement get(String fieldName);

	public Iterator<String> getFieldNames();
}

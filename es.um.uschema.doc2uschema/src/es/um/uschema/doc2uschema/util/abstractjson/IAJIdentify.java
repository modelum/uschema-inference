package es.um.uschema.doc2uschema.util.abstractjson;

/**
 * @author dsevilla
 *
 */
public interface IAJIdentify
{
	public boolean isArray();

	public boolean isObject();

	public boolean isNumber();

	public boolean isTextual();

	public boolean isBoolean();

	public boolean isNull();

	public boolean isObjectId();

	public IAJArray asArray();

	public IAJObject asObject();

	public String asString();

	public Long asLong();

	public IAJTextual asTextual();

	public IAJBoolean asBoolean();

	public IAJNull asNull();

	public IAJNumber asNumber();

	public IAJObjectId asObjectId();
}

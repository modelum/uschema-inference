/**
 *
 */
package automated.builder;

import java.util.Optional;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchemaFactory;
import es.um.uschema.USchema.USchema;


/**
 * @author dsevilla
 *
 */
public class UBuilder
{
	private USchema schema;

	@SuppressWarnings("unused")
	private Optional<Attribute> tmpAtt;
	private Optional<EntityType> tmpEntity;
	private Optional<StructuralVariation> tmpVariation;

	public UBuilder(USchema uSchema)
	{
		schema = uSchema;

		tmpAtt = Optional.empty();
		tmpEntity = Optional.empty();
		tmpVariation = Optional.empty();
	}

	static public UBuilder schema(String name)
	{
	  USchema s = USchemaFactory.eINSTANCE.createUSchema();
		s.setName(name);
		return new UBuilder(s);
	}

	public UBuilder entity(String name)
	{
		tmpEntity.ifPresent(e -> endEntity());
		EntityType et = USchemaFactory.eINSTANCE.createEntityType();
		et.setName(name);
		tmpEntity = Optional.of(et);
		return this;
	}

	public UBuilder variation(int id)
	{
		tmpVariation.ifPresent(v -> endVariation());
		StructuralVariation sv = USchemaFactory.eINSTANCE.createStructuralVariation();
		sv.setVariationId(id);
		tmpVariation = Optional.of(sv);
		return this;
	}

	// Primitive
	public UBuilder attribute(String name, String type)
	{
		Attribute at = USchemaFactory.eINSTANCE.createAttribute();
		at.setName(name);

		// Create the datatype for the attribute
		PrimitiveType t = USchemaFactory.eINSTANCE.createPrimitiveType();
		t.setName(type);
		at.setType(t);
		tmpAtt = Optional.of(at);

		tmpVariation.get().getFeatures().add(at);
		tmpVariation.get().getStructuralFeatures().add(at);
		return this;
	}

	public UBuilder endVariation()
	{
		endAttribute();
		tmpEntity.get().getVariations().add(tmpVariation.get());
		cleanVariation();
		return this;
	}

	public UBuilder endEntity()
	{
		endVariation();
		schema.getEntities().add(tmpEntity.get());
		cleanEntity();
		return this;
	}

	public UBuilder endAttribute()
	{
		cleanAttribute();
		return this;
	}

	private void cleanEntity()
	{
		cleanVariation();
		tmpEntity = Optional.empty();
	}

	private void cleanVariation()
	{
		cleanAttribute();
		tmpVariation = Optional.empty();
	}

	private void cleanAttribute()
	{
		tmpAtt = Optional.empty();
	}

	public USchema build()
	{
		tmpEntity.ifPresent(e -> endEntity());
		return schema;
	}
}

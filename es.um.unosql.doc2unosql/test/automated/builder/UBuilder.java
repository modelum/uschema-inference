/**
 *
 */
package automated.builder;

import java.util.Optional;

import es.um.unosql.uNoSQLSchema.Attribute;
import es.um.unosql.uNoSQLSchema.EntityType;
import es.um.unosql.uNoSQLSchema.PrimitiveType;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.UNoSQLSchemaFactory;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;


/**
 * @author dsevilla
 *
 */
public class UBuilder
{
	private uNoSQLSchema schema;

	@SuppressWarnings("unused")
	private Optional<Attribute> tmpAtt;
	private Optional<EntityType> tmpEntity;
	private Optional<StructuralVariation> tmpVariation;

	public UBuilder(uNoSQLSchema uSchema)
	{
		schema = uSchema;

		tmpAtt = Optional.empty();
		tmpEntity = Optional.empty();
		tmpVariation = Optional.empty();
	}

	static public UBuilder schema(String name)
	{
		uNoSQLSchema s = UNoSQLSchemaFactory.eINSTANCE.createuNoSQLSchema();
		s.setName(name);
		return new UBuilder(s);
	}

	public UBuilder entity(String name)
	{
		tmpEntity.ifPresent(e -> endEntity());
		EntityType et = UNoSQLSchemaFactory.eINSTANCE.createEntityType();
		et.setName(name);
		tmpEntity = Optional.of(et);
		return this;
	}

	public UBuilder variation(int id)
	{
		tmpVariation.ifPresent(v -> endVariation());
		StructuralVariation sv = UNoSQLSchemaFactory.eINSTANCE.createStructuralVariation();
		sv.setVariationId(id);
		tmpVariation = Optional.of(sv);
		return this;
	}

	// Primitive
	public UBuilder attribute(String name, String type)
	{
		Attribute at = UNoSQLSchemaFactory.eINSTANCE.createAttribute();
		at.setName(name);

		// Create the datatype for the attribute
		PrimitiveType t = UNoSQLSchemaFactory.eINSTANCE.createPrimitiveType();
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

	public uNoSQLSchema build()
	{
		tmpEntity.ifPresent(e -> endEntity());
		return schema;
	}
}

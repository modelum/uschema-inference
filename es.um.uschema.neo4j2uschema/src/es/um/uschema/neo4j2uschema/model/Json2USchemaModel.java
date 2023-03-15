package es.um.uschema.neo4j2uschema.model;

import static es.um.uschema.neo4j2uschema.constants.Constants.SLASH;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.json.JSONObject;

import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.neo4j2uschema.model.builder.USchemaBuilder;
import es.um.uschema.neo4j2uschema.model.builder.StructuralVariationBuilder;
import es.um.uschema.neo4j2uschema.model.repository.UModelRepository;

public class Json2USchemaModel
{	
	private String databaseName;
	
	private UModelRepository modelRepository;
	private USchemaBuilder uBuilder;
	private StructuralVariationBuilder variationBuilder;

	private AttributeOptionalsChecker attributeOptionalsChecker;
	private IgnoreSimilarReferenceBoundsProcessor similarReferenceProcessor;

	public Json2USchemaModel(String databaseName)
	{
		this.databaseName = databaseName;
		
		this.modelRepository = new UModelRepository();
		this.uBuilder = new USchemaBuilder(modelRepository);
		this.variationBuilder = new StructuralVariationBuilder(uBuilder, modelRepository);
		this.attributeOptionalsChecker = new AttributeOptionalsChecker(modelRepository);
		this.similarReferenceProcessor = new IgnoreSimilarReferenceBoundsProcessor(modelRepository);
	}

	public void processArchetypes(Map<String, Long> archetypesCounts)
	{
	  uBuilder.createUSchema(databaseName);
		
		archetypesCounts.entrySet().forEach(archetypeCountEntry -> {
			JSONObject entityArchetypeJson = new JSONObject(archetypeCountEntry.getKey());
			variationBuilder.createVariation(entityArchetypeJson, archetypeCountEntry.getValue());
		});
		
		attributeOptionalsChecker.processOptionals();
		if (true)
			similarReferenceProcessor.joinVariationsIgnoringBounds();
	}

	public void toXMI(String outputUri) {
		new File(outputUri.substring(0, outputUri.lastIndexOf(SLASH))).mkdirs();
		
		EcoreModelIO uSchemaWriter = new EcoreModelIO();
		uSchemaWriter.write(modelRepository.getUSchema(), Path.of(outputUri));
	}
	
	public USchema getUSchema() {
		return modelRepository.getUSchema();
	}

	
}
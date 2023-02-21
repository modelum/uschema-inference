package es.um.unosql.neo4j2unosql.model;

import static es.um.unosql.neo4j2unosql.constants.Constants.SLASH;

import java.io.File;
import java.util.Map;

import org.json.JSONObject;

import es.um.unosql.neo4j2unosql.model.builder.NoSQLSchemaBuilder;
import es.um.unosql.neo4j2unosql.model.builder.StructuralVariationBuilder;
import es.um.unosql.neo4j2unosql.model.repository.NoSQLModelRepository;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import es.um.unosql.utils.UNoSQLSchemaWriter;

public class Json2NoSQLSchemaModel
{	
	private String databaseName;
	
	private NoSQLModelRepository modelRepository;
	private NoSQLSchemaBuilder noSQLBuilder;
	private StructuralVariationBuilder variationBuilder;

	private AttributeOptionalsChecker attributeOptionalsChecker;
	private IgnoreSimilarReferenceBoundsProcessor similarReferenceProcessor;

	public Json2NoSQLSchemaModel(String databaseName)
	{
		this.databaseName = databaseName;
		
		this.modelRepository = new NoSQLModelRepository();
		this.noSQLBuilder = new NoSQLSchemaBuilder(modelRepository);
		this.variationBuilder = new StructuralVariationBuilder(noSQLBuilder, modelRepository);
		this.attributeOptionalsChecker = new AttributeOptionalsChecker(modelRepository);
		this.similarReferenceProcessor = new IgnoreSimilarReferenceBoundsProcessor(modelRepository);
	}

	public void processArchetypes(Map<String, Long> archetypesCounts)
	{
		noSQLBuilder.createNoSQLSchema(databaseName);
		
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
		
		UNoSQLSchemaWriter noSQLSchemaWriter = new UNoSQLSchemaWriter();
		noSQLSchemaWriter.write(modelRepository.getNoSQLSchema(), new File(outputUri));
	}
	
	public uNoSQLSchema getuNoSQLSchema() {
		return modelRepository.getNoSQLSchema();
	}

	
}
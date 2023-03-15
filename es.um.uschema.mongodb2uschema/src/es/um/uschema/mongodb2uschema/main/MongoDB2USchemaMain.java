package es.um.uschema.mongodb2uschema.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import es.um.uschema.doc2uschema.process.util.AliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultAliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultEVariationMerger;
import es.um.uschema.doc2uschema.process.util.DefaultOptionalTagger;
import es.um.uschema.doc2uschema.process.util.DefaultReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.EVariationMerger;
import es.um.uschema.doc2uschema.process.util.OptionalTagger;
import es.um.uschema.doc2uschema.process.util.ReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.StructuralVariationSorter;
import es.um.uschema.doc2uschema.util.config.DefaultSchemaInferenceConfig;
import es.um.uschema.doc2uschema.util.config.SchemaInferenceConfig;
import es.um.uschema.doc2uschema.process.util.DefaultStructuralVariationSorter;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.mongodb2uschema.MongoDB2USchema;
import es.um.uschema.mongodb2uschema.utils.Constants;
import es.um.uschema.doc2uschema.util.abstractjson.IAJAdapter;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;
import es.um.uschema.doc2uschema.util.abstractjson.impl.jackson.JacksonAdapter;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

public class MongoDB2USchemaMain extends AbstractModule
{

	public static void main(String args[]) throws IOException, JSONException 
	{
		Injector injector = Guice.createInjector(new MongoDB2USchemaMain());
		MongoDB2USchemaMain m = injector.getInstance(MongoDB2USchemaMain.class);
	    m.run(args);
	}
	
	@Inject private MongoDB2USchema schemaBuilder;
	
	public void run(String[] args) throws IOException, JSONException
    {
		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) 
			prop.load(inputStream);
		else
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		
		String url = prop.getProperty(Constants.CONFIG_MONGODB_CONNECTION_KEY);
		String database = prop.getProperty(Constants.CONFIG_MONGODB_DATABASE_KEY);
		String[] collections = prop.getProperty(Constants.CONFIG_MONGODB_COLLECTIONS_KEY).split(",");
		schemaBuilder.process(url, database, collections);
    }
	
	
	@Override
	protected void configure()
	{
	  // Default config
	  bind(StructuralVariationSorter.class).to(DefaultStructuralVariationSorter.class);
	  bind(OptionalTagger.class).to(DefaultOptionalTagger.class);
	  bind(new TypeLiteral<ReferenceMatcherCreator<EntityType>>() {}).to(DefaultReferenceMatcherCreator.class);
	
	  // Schema inference
	  bind(AliasedAggregatedEntityJoiner.class).to(DefaultAliasedAggregatedEntityJoiner.class);
	  bind(EVariationMerger.class).to(DefaultEVariationMerger.class);
	  bind(SchemaInferenceConfig.class).to(DefaultSchemaInferenceConfig.class);
	  
	  // Adapter
	  bind(IAJAdapter.class).to(JacksonAdapter.class);
	}
	
}

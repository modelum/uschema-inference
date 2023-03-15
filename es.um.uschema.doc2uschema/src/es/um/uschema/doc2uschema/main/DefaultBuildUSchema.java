package es.um.uschema.doc2uschema.main;

import java.io.File;
import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.doc2uschema.process.util.AliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultAliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultEVariationMerger;
import es.um.uschema.doc2uschema.process.util.DefaultFeatureAnalyzer;
import es.um.uschema.doc2uschema.process.util.DefaultOptionalTagger;
import es.um.uschema.doc2uschema.process.util.DefaultReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.EVariationMerger;
import es.um.uschema.doc2uschema.process.util.FeatureAnalyzer;
import es.um.uschema.doc2uschema.process.util.NullStructuralVariationSorter;
import es.um.uschema.doc2uschema.process.util.OptionalTagger;
import es.um.uschema.doc2uschema.process.util.ReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.StructuralVariationSorter;
import es.um.uschema.doc2uschema.util.abstractjson.except.JSONException;
import es.um.uschema.doc2uschema.util.config.DefaultSchemaInferenceConfig;
import es.um.uschema.doc2uschema.util.config.SchemaInferenceConfig;

public class DefaultBuildUSchema extends AbstractModule
{
    public static void main(String[] args) throws IOException, JSONException
    {
        if (args.length < 2)
        {
            System.err.println("At least the JSON file and the output must be specified.");
            System.err.println("Usage: BuildUSchema JSONfile outputXMIfile");
            System.exit(-1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        if (outputFile.getParentFile() != null)
            outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();

        BuildUSchema builder = DefaultBuildUSchema.getInjectedInstance();
        builder.buildFromFile(inputFile);
        builder.writeToFile(outputFile);
    }

    public static BuildUSchema getInjectedInstance()
    {
        Injector injector = Guice.createInjector(new DefaultBuildUSchema());
        DefaultBuildUSchema injectedInstance = injector.getInstance(DefaultBuildUSchema.class);

        return injectedInstance.builder;
    }

    @Inject
    BuildUSchema builder;

    @Override
    protected void configure()
    {
        // Default config
        bind(StructuralVariationSorter.class).to(NullStructuralVariationSorter.class);
        bind(OptionalTagger.class).to(DefaultOptionalTagger.class);
        bind(FeatureAnalyzer.class).to(DefaultFeatureAnalyzer.class);
        bind(new TypeLiteral<ReferenceMatcherCreator<EntityType>>() {
        }).to(DefaultReferenceMatcherCreator.class);

        // Schema inference
        bind(AliasedAggregatedEntityJoiner.class).to(DefaultAliasedAggregatedEntityJoiner.class);
        bind(EVariationMerger.class).to(DefaultEVariationMerger.class);
        bind(SchemaInferenceConfig.class).to(DefaultSchemaInferenceConfig.class);
    }
}

package es.um.unosql.doc2unosql.main;

import java.io.File;
import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import es.um.unosql.doc2unosql.process.util.AliasedAggregatedEntityJoiner;
import es.um.unosql.doc2unosql.process.util.DefaultAliasedAggregatedEntityJoiner;
import es.um.unosql.doc2unosql.process.util.DefaultEVariationMerger;
import es.um.unosql.doc2unosql.process.util.DefaultFeatureAnalyzer;
import es.um.unosql.doc2unosql.process.util.DefaultOptionalTagger;
import es.um.unosql.doc2unosql.process.util.DefaultReferenceMatcherCreator;
import es.um.unosql.doc2unosql.process.util.EVariationMerger;
import es.um.unosql.doc2unosql.process.util.FeatureAnalyzer;
import es.um.unosql.doc2unosql.process.util.NullStructuralVariationSorter;
import es.um.unosql.doc2unosql.process.util.OptionalTagger;
import es.um.unosql.doc2unosql.process.util.ReferenceMatcherCreator;
import es.um.unosql.doc2unosql.process.util.StructuralVariationSorter;
import es.um.unosql.doc2unosql.util.abstractjson.except.JSONException;
import es.um.unosql.doc2unosql.util.config.DefaultSchemaInferenceConfig;
import es.um.unosql.doc2unosql.util.config.SchemaInferenceConfig;
import es.um.unosql.uNoSQLSchema.EntityType;

public class DefaultBuildUNoSQLSchema extends AbstractModule
{
    public static void main(String[] args) throws IOException, JSONException
    {
        if (args.length < 2)
        {
            System.err.println("At least the JSON file and the output must be specified.");
            System.err.println("Usage: BuildNoSQLSchema JSONfile outputXMIfile");
            System.exit(-1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        if (outputFile.getParentFile() != null)
            outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();

        BuildUNoSQLSchema builder = DefaultBuildUNoSQLSchema.getInjectedInstance();
        builder.buildFromFile(inputFile);
        builder.writeToFile(outputFile);
    }

    public static BuildUNoSQLSchema getInjectedInstance()
    {
        Injector injector = Guice.createInjector(new DefaultBuildUNoSQLSchema());
        DefaultBuildUNoSQLSchema injectedInstance = injector.getInstance(DefaultBuildUNoSQLSchema.class);

        return injectedInstance.builder;
    }

    @Inject
    BuildUNoSQLSchema builder;

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

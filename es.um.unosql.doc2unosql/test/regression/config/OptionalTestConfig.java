package regression.config;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import es.um.unosql.doc2unosql.main.BuildUNoSQLSchema;
import es.um.unosql.doc2unosql.process.util.AliasedAggregatedEntityJoiner;
import es.um.unosql.doc2unosql.process.util.DefaultAliasedAggregatedEntityJoiner;
import es.um.unosql.doc2unosql.process.util.DefaultEVariationMerger;
import es.um.unosql.doc2unosql.process.util.DefaultOptionalTagger;
import es.um.unosql.doc2unosql.process.util.DefaultReferenceMatcherCreator;
import es.um.unosql.doc2unosql.process.util.EVariationMerger;
import es.um.unosql.doc2unosql.process.util.NullStructuralVariationSorter;
import es.um.unosql.doc2unosql.process.util.OptionalTagger;
import es.um.unosql.doc2unosql.process.util.ReferenceMatcherCreator;
import es.um.unosql.doc2unosql.process.util.StructuralVariationSorter;
import es.um.unosql.doc2unosql.util.config.DefaultSchemaInferenceConfig;
import es.um.unosql.doc2unosql.util.config.SchemaInferenceConfig;
import es.um.unosql.uNoSQLSchema.EntityType;

public class OptionalTestConfig extends AbstractModule
{
  private BuildUNoSQLSchema schema = null;
  private Injector injector;

  public OptionalTestConfig()
  {
    injector = Guice.createInjector(this);
  }

  public BuildUNoSQLSchema getUNoSQLSchema()
  {
    if (schema == null)
      schema = injector.getInstance(BuildUNoSQLSchema.class);

    return schema;
  }

  @Override
  protected void configure()
  {
    // Default config
    bind(StructuralVariationSorter.class).to(NullStructuralVariationSorter.class);
    bind(OptionalTagger.class).to(DefaultOptionalTagger.class);
    bind(new TypeLiteral<ReferenceMatcherCreator<EntityType>>() {}).to(DefaultReferenceMatcherCreator.class);

    // Schema inference
    bind(AliasedAggregatedEntityJoiner.class).to(DefaultAliasedAggregatedEntityJoiner.class);
    bind(EVariationMerger.class).to(DefaultEVariationMerger.class);
    bind(SchemaInferenceConfig.class).to(DefaultSchemaInferenceConfig.class);
  }
}

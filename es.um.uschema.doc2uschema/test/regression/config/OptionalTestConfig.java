package regression.config;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.doc2uschema.main.BuildUSchema;
import es.um.uschema.doc2uschema.process.util.AliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultAliasedAggregatedEntityJoiner;
import es.um.uschema.doc2uschema.process.util.DefaultEVariationMerger;
import es.um.uschema.doc2uschema.process.util.DefaultOptionalTagger;
import es.um.uschema.doc2uschema.process.util.DefaultReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.EVariationMerger;
import es.um.uschema.doc2uschema.process.util.NullStructuralVariationSorter;
import es.um.uschema.doc2uschema.process.util.OptionalTagger;
import es.um.uschema.doc2uschema.process.util.ReferenceMatcherCreator;
import es.um.uschema.doc2uschema.process.util.StructuralVariationSorter;
import es.um.uschema.doc2uschema.util.config.DefaultSchemaInferenceConfig;
import es.um.uschema.doc2uschema.util.config.SchemaInferenceConfig;

public class OptionalTestConfig extends AbstractModule
{
  private BuildUSchema schema = null;
  private Injector injector;

  public OptionalTestConfig()
  {
    injector = Guice.createInjector(this);
  }

  public BuildUSchema getUSchema()
  {
    if (schema == null)
      schema = injector.getInstance(BuildUSchema.class);

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

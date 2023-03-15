package es.um.uschema.doc2uschema.validation.main;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.RelationshipType;
import es.um.uschema.USchema.SchemaType;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.utils.custom.compare.CompareSchemaType;
import es.um.uschema.utils.custom.compare.CompareStructuralVariation;

public class USchemaCompareMain
{
  private static final boolean LOWERCASE_NAMES = true;
  private static final Function<String,String> casec = (n -> LOWERCASE_NAMES ? n.toLowerCase() : n);
  private List<String> hitLog = new ArrayList<String>();
  private List<String> warningLog = new ArrayList<String>();
  private CompareStructuralVariation varComparer = new CompareStructuralVariation();

  public boolean hasHits()
  {
    return !this.hitLog.isEmpty();
  }

  public boolean hasWarnings()
  {
    return !this.warningLog.isEmpty();
  }

  public List<String> getHitLog()
  {
    return this.hitLog;
  }

  public List<String> getWarningLog()
  {
    return this.warningLog;
  }

  public void startComparison(USchema schema1, USchema schema2)
  {
    // First of all we compare schema names
    if (!schema1.getName().equals(schema2.getName()))
      warningLog.add("Schema warning: Names do not match: " + "(" + schema1.getName() + ", " + schema2.getName() + ")");

    // For EntityTypes and RelationshipTypes we compare that both Schemas do have the same number of elements...
    if (schema1.getEntities().size() != schema2.getEntities().size())
      warningLog.add("Schema warning: EntityType lists sizes do not match: Schema1 (" + schema1.getEntities().size() + "), Schema2 (" + schema2.getEntities().size() + ").");

    // For each EntityType in Schema1 we need to match an equivalent EntityType in Schema2.
    for (EntityType e1 : schema1.getEntities())
    {
      String name1 = casec.apply(e1.getName());

      // We try to find a SchemaType in Schema2 with the same name...
      Optional<EntityType> eType = schema2.getEntities().stream()
    		  .filter(e2 -> name1.equals(casec.apply(e2.getName()))).findAny();

      if (!eType.isPresent())
      {
    	  Optional<EntityType> possibleMatch = schema2.getEntities().stream()
			  .filter(e2 -> CompareSchemaType.compareNames(e1, e2))
			  .findAny();
    	  if (possibleMatch.isPresent()) {
    		  EntityType possibleEType = possibleMatch.get();
			if (compareSchemaTypes(e1, possibleEType))
    	       hitLog.add("EntityType hit: " + e1.getName() + " not found, used " + possibleEType.getName() + " instead.");
			 else
	    	   warningLog.add("EntityType warning: " + e1.getName() + " entity type name could not be found in Schema2");
    	  } else 
    		  warningLog.add("EntityType warning: " + e1.getName() + " entity type name could not be found in Schema2");
      }
      else
      {
        // If we found it, then we perform an in-depth comparison (check method below).
        if (compareSchemaTypes(e1, eType.get()))
          hitLog.add("EntityType hit: " + e1.getName());
      }
    }

    // Same here as before but with RelationshipTypes.
    if (schema1.getRelationships().size() != schema2.getRelationships().size())
      warningLog.add("Schema warning: Relationships lists sizes do not match: Schema1 (" + schema1.getRelationships().size() + "), Schema2 (" + schema2.getRelationships().size() + ").");

    for (RelationshipType r1 : schema1.getRelationships())
    {
      String name1 = casec.apply(r1.getName());

      Optional<RelationshipType> rType = schema2.getRelationships().stream()
    		  .filter(r2 -> name1.equals(casec.apply(r2.getName()))).findAny();

      if (!rType.isPresent())
        warningLog.add("RelationshipType warning: " + r1.getName() + " relationship type name could not be found in Schema2.");
      else
      {
        if(compareSchemaTypes(r1, rType.get()))
          hitLog.add("RelationshipType hit: " + r1.getName());
      }
    }
  }

  private boolean compareSchemaTypes(SchemaType s1, SchemaType s2)
  {
    // So here we have two SchemaTypes with the same names.
    boolean goodHit = true;

    // To compare SchemaTypes we check that they have the same number of variations.
    if (s1.getVariations().size() != s2.getVariations().size())
    {
      warningLog.add("SchemaType warning: Variation lists sizes do not match: " + s1.getName() + "(" + s1.getVariations().size() + "), " + s2.getName() + "(" + s2.getVariations().size() + ")");
      goodHit = false;
    }

    // Now for each variation in SchemaType1 we search for an equivalent variation in SchemaType2.
    for (StructuralVariation v1 : s1.getVariations())
    {
      // Find a variation with exactly the same features as v1.
      Optional<StructuralVariation> varOption = s2.getVariations().stream().filter(v2 -> varComparer.compare(v1, v2)).findAny();
      if (varOption.isPresent())
        hitLog.add("VariationType hit: (Sch1) " + s1.getName() + "." + v1.getVariationId() + " == (Sch2) " + s2.getName() + "." + varOption.get().getVariationId());
      else
      {
        warningLog.add("VariationType warning: " + s1.getName() + "." + v1.getVariationId() + " is not matched by any variation in " + s2.getName() + " in Schema2");
        goodHit = false;
      }
    }

    // If each and every variation in SchemaType1 was matched, then the SchemaType1 as a whole was validated.
    return goodHit;
  }


  public static void main(String[] args)
  {
    String model1 = "../es.um.uschema.models/userprofile/userprofile_neo4j.xmi";
    String model2 = "./inputs/userProfile_neo4j_min.xmi";

    EcoreModelIO loader = new EcoreModelIO();
    USchema schema1 = loader.load(USchema.class, Path.of(model1));
    USchema schema2 = loader.load(USchema.class, Path.of(model2));

    USchemaCompareMain comparer = new USchemaCompareMain();
    comparer.startComparison(schema1, schema2);

    if (comparer.hasWarnings())
    {
      System.out.println("Some warnings were found during comparison:");
      System.out.println("  " + comparer.getWarningLog().stream().collect(Collectors.joining("\n  ")));
    }

    if (comparer.hasHits())
    {
      System.out.println("\nHits found during comparison:");
      System.out.println("  " + comparer.getHitLog().stream().collect(Collectors.joining("\n  ")));
    }
  }
}

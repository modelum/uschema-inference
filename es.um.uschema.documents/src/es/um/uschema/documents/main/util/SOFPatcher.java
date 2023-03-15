package es.um.uschema.documents.main.util;

import java.nio.file.Path;

import org.eclipse.emf.common.util.EList;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.utils.USchemaFactory;

public class SOFPatcher
{
  private USchemaFactory factory = new USchemaFactory();

  public static void main(String[] args)
  {
    SOFPatcher patcher = new SOFPatcher();
    patcher.patch("../es.um.uschema.xtext.models/thesis/subtypes/stackoverflow.xmi");
  }

  public void patch(String modelRoute)
  {
    EcoreModelIO modelIO = new EcoreModelIO();
    USchema schema = modelIO.load(USchema.class, Path.of(modelRoute));

    for (EntityType e : schema.getEntities())
      switch (e.getName())
      {
        case "Users": {patchUsers(e.getVariations()); break;}
        case "Badges": {patchBadges(e.getVariations()); break;}
        case "Tags": {patchTags(e.getVariations()); break;}
        case "Postlinks": {patchPostlinks(e.getVariations()); break;}
        case "Votes": {patchVotes(e.getVariations()); break;}
        case "Comments": {patchComments(e.getVariations()); break;}
        case "Posts": {patchPosts(e.getVariations()); break;}
      }

    modelIO.write(schema, Path.of(modelRoute));
  }

  private void patchUsers(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmUpVotes = null;
      Attribute rmDownVotes = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("AccountId") || f.getName().equals("Age") || f.getName().equals("Reputation"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("UpVotes"))
          rmUpVotes = (Attribute)f;
        else if (f.getName().equals("DownVotes"))
          rmDownVotes = (Attribute)f;

      if (rmUpVotes != null)
      {
        sv.getLogicalFeatures().removeAll(rmUpVotes.getReferences());
        sv.getFeatures().removeAll(rmUpVotes.getReferences());
        rmUpVotes.setType(factory.createPrimitiveType("Number"));
        rmUpVotes.getReferences().clear();
      }
      if (rmDownVotes != null)
      {
        sv.getLogicalFeatures().removeAll(rmDownVotes.getReferences());
        sv.getFeatures().removeAll(rmDownVotes.getReferences());
        rmDownVotes.setType(factory.createPrimitiveType("Number"));
        rmDownVotes.getReferences().clear();
      }
    }
  }

  private void patchBadges(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmTagBased = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("Class"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("TagBased"))
          rmTagBased = (Attribute)f;

      if (rmTagBased != null)
      {
        sv.getLogicalFeatures().removeAll(rmTagBased.getReferences());
        sv.getFeatures().removeAll(rmTagBased.getReferences());
        rmTagBased.getReferences().clear();
      }
    }
  }

  private void patchTags(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmTagName = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("Count"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("TagName"))
          rmTagName = (Attribute)f;

      if (rmTagName != null)
      {
        sv.getLogicalFeatures().removeAll(rmTagName.getReferences());
        sv.getFeatures().removeAll(rmTagName.getReferences());
        rmTagName.getReferences().clear();
      }
    }
  }

  private void patchPostlinks(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("LinkTypeId"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
  }

  private void patchVotes(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmVoteTypeId = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("BountyAmount"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("VoteTypeId"))
          rmVoteTypeId = (Attribute)f;

      if (rmVoteTypeId != null)
      {
        sv.getLogicalFeatures().removeAll(rmVoteTypeId.getReferences());
        sv.getFeatures().removeAll(rmVoteTypeId.getReferences());
        rmVoteTypeId.setType(factory.createPrimitiveType("Number"));
        rmVoteTypeId.getReferences().clear();
      }
    }
  }

  private void patchComments(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmUserDisplayName = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("Score"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("UserDisplayName"))
          rmUserDisplayName = (Attribute)f;

      if (rmUserDisplayName != null)
      {
        sv.getLogicalFeatures().removeAll(rmUserDisplayName.getReferences());
        sv.getFeatures().removeAll(rmUserDisplayName.getReferences());
        rmUserDisplayName.getReferences().clear();
      }
    }
  }

  private void patchPosts(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Attribute rmPostTypeId = null;

      for (Feature f : sv.getFeatures())
        if (f.getName() == null)
          continue;
        else if (f.getName().equals("AnswerCount") || f.getName().equals("Score") || f.getName().equals("CommentCount") || f.getName().equals("FavoriteCount") || f.getName().equals("ViewCount"))
          ((Attribute)f).setType(factory.createPrimitiveType("Number"));
        else if (f.getName().equals("PostTypeId"))
          rmPostTypeId = (Attribute)f;

      if (rmPostTypeId != null)
      {
        sv.getLogicalFeatures().removeAll(rmPostTypeId.getReferences());
        sv.getFeatures().removeAll(rmPostTypeId.getReferences());
        rmPostTypeId.getReferences().clear();
        rmPostTypeId.setType(factory.createPrimitiveType("Number"));
      }
    }
  }
}

package es.um.uschema.documents.main.util;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;

import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.EntityType;
import es.um.uschema.USchema.USchema;
import es.um.uschema.utils.EcoreModelIO;
import es.um.uschema.utils.USchemaFactory;
import es.um.uschema.USchema.Feature;
import es.um.uschema.USchema.Reference;
import es.um.uschema.USchema.StructuralVariation;

public class RedditPatcher
{
  private USchemaFactory factory = new USchemaFactory();

  public static void main(String[] args)
  {
    RedditPatcher patcher = new RedditPatcher();
    patcher.patch("../es.um.uschema.models/reddit/reddit.xmi");
  }

  public void patch(String modelRoute)
  {
    EcoreModelIO modelIO = new EcoreModelIO();
    USchema schema = modelIO.load(USchema.class, Path.of(modelRoute));

    for (EntityType e : schema.getEntities())
      switch (e.getName())
      {
        case "Comments": {patchComments(e.getVariations()); break;}
        case "Authors": {patchAuthors(e.getVariations()); break;}
        case "Subreddits": {patchSubreddits(e.getVariations()); break;}
        case "Moderators": {patchModerators(e.getVariations()); break;}
      }

    modelIO.write(schema, Path.of(modelRoute));
  }

  private void patchComments(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Optional<Feature> rmSubreddit = sv.getFeatures().stream().filter(f -> "subreddit".equals(f.getName())).findAny();
      Optional<Feature> rmAuthor_flair_css_class = sv.getFeatures().stream().filter(f -> "author_flair_css_class".equals(f.getName())).findAny();
      Optional<Feature> rmAuthor_flair_text = sv.getFeatures().stream().filter(f-> "author_flair_text".equals(f.getName())).findAny();
      Optional<Feature> rmParent = sv.getFeatures().stream().filter(f -> "parent_id".equals(f.getName())).findAny();

      if (rmSubreddit.isPresent())
      {
        Attribute subreddit = (Attribute)rmSubreddit.get();
        sv.getLogicalFeatures().removeAll(subreddit.getReferences());
        sv.getFeatures().removeAll(subreddit.getReferences());
        subreddit.getReferences().clear();
      }
      if (rmAuthor_flair_css_class.isPresent())
      {
        Attribute author_flair_css_class = (Attribute)rmAuthor_flair_css_class.get();
        sv.getLogicalFeatures().removeAll(author_flair_css_class.getReferences());
        sv.getFeatures().removeAll(author_flair_css_class.getReferences());
        author_flair_css_class.getReferences().clear();
      }
      if (rmAuthor_flair_text.isPresent())
      {
        Attribute author_flair_text = (Attribute)rmAuthor_flair_text.get();
        sv.getLogicalFeatures().removeAll(author_flair_text.getReferences());
        sv.getFeatures().removeAll(author_flair_text.getReferences());
        author_flair_text.getReferences().clear();
      }
      if (rmParent.isPresent())
      {
        Attribute parent = (Attribute)rmParent.get();
        Reference ref = factory.createReference(null, 1, 1, ((EntityType)((StructuralVariation)parent.eContainer()).getContainer()));
        ref.getAttributes().add(parent);
        parent.getReferences().add(ref);
        sv.getFeatures().add(ref);
        sv.getLogicalFeatures().add(ref);
      }
    }
  }

  private void patchAuthors(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Optional<Feature> rmCommentKarma = sv.getFeatures().stream().filter(f -> f.getName() != null && f.getName().equals("comment_karma")).findAny();

      if (rmCommentKarma.isPresent())
      {
        Attribute commentKarma = (Attribute)rmCommentKarma.get();
        sv.getLogicalFeatures().removeAll(commentKarma.getReferences());
        sv.getFeatures().removeAll(commentKarma.getReferences());
        commentKarma.setType(factory.createPrimitiveType("Number"));
        commentKarma.getReferences().clear();
      }
    }
  }

  private void patchSubreddits(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Optional<Feature> rmSubredditType = sv.getFeatures().stream().filter(f -> "subreddit_type".equals(f.getName())).findAny();
      Optional<Feature> rmHidden = sv.getFeatures().stream().filter(f -> "comment_score_hide_mins".equals(f.getName())).findAny();
      Optional<Feature> rmComments = sv.getFeatures().stream().filter(f -> "collapse_deleted_comments".equals(f.getName())).findAny();

      if (rmSubredditType.isPresent())
      {
        Attribute subredditType = (Attribute)rmSubredditType.get();
        sv.getLogicalFeatures().removeAll(subredditType.getReferences());
        sv.getFeatures().removeAll(subredditType.getReferences());
        subredditType.getReferences().clear();
      }
      if (rmHidden.isPresent())
      {
        Attribute hidden = (Attribute)rmHidden.get();
        sv.getLogicalFeatures().removeAll(hidden.getReferences());
        sv.getFeatures().removeAll(hidden.getReferences());
        hidden.getReferences().clear();
      }
      if (rmComments.isPresent())
      {
        Attribute comments = (Attribute)rmComments.get();
        sv.getLogicalFeatures().removeAll(comments.getReferences());
        sv.getFeatures().removeAll(comments.getReferences());
        comments.getReferences().clear();
      }
    }
  }

  private void patchModerators(EList<StructuralVariation> variations)
  {
    for (StructuralVariation sv : variations)
    {
      Optional<Feature> rmSubredditName = sv.getFeatures().stream().filter(f -> f.getName() != null && f.getName().equals("subreddit_name")).findAny();
      Optional<Feature> rmSubredditType = sv.getFeatures().stream().filter(f -> f.getName() != null && f.getName().equals("subreddit_type")).findAny();
      Optional<Feature> rmUserData = sv.getFeatures().stream().filter(f -> f.getName() != null && f.getName().equals("user_data")).findAny();

      if (rmSubredditName.isPresent())
      {
        Attribute subredditName = (Attribute)rmSubredditName.get();
        sv.getLogicalFeatures().removeAll(subredditName.getReferences());
        sv.getFeatures().removeAll(subredditName.getReferences());
        subredditName.getReferences().clear();
      }
      if (rmSubredditType.isPresent())
      {
        Attribute subredditName = (Attribute)rmSubredditType.get();
        sv.getLogicalFeatures().removeAll(subredditName.getReferences());
        sv.getFeatures().removeAll(subredditName.getReferences());
        subredditName.getReferences().clear();
      }
      if (rmUserData.isPresent())
      {
        Attribute userData = (Attribute)rmUserData.get();
        Reference ref = factory.createReference(null, 1, 1, ((EntityType)((StructuralVariation)userData.eContainer()).getContainer()));
        ref.getAttributes().add(userData);
        userData.getReferences().add(ref);
        sv.getFeatures().add(ref);
        sv.getLogicalFeatures().add(ref);
      }
    }
  }
}

package edu.cmu.cs.ziy.wiki.entity;

import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity.Relation;

public class BoldTextExpander extends AbstractContentBasedExpander implements
        WikipediaEntityExpander {

  private static String BOLDTEXT_FIND_PATTERN = "'{3}([^\\[\\]\\|]+?)'{3}";

  @Override
  public String getTextExtractionPattern() {
    return BOLDTEXT_FIND_PATTERN;
  }

  @Override
  public Relation getEntityRelation() {
    return WikipediaEntity.Relation.BOLDTEXT;
  }
}

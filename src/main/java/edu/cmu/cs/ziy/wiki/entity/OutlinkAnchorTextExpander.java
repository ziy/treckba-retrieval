package edu.cmu.cs.ziy.wiki.entity;

import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity.Relation;

public class OutlinkAnchorTextExpander extends AbstractContentBasedExpander implements
        WikipediaEntityExpander {

  private static String ANCHOR_FIND_PATTERN = "\\[{2}(?:.+?)(?:\\|(.*?))?\\]{2}";

  @Override
  public String getTextExtractionPattern() {
    return ANCHOR_FIND_PATTERN;
  }

  @Override
  public Relation getEntityRelation() {
    return WikipediaEntity.Relation.OUTLINK_ANCHOR;
  }
}

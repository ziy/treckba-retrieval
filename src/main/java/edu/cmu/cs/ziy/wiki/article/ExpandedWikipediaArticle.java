package edu.cmu.cs.ziy.wiki.article;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Sets;

import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity;

public class ExpandedWikipediaArticle extends WikipediaArticle implements Serializable {

  private static final long serialVersionUID = 1L;

  protected Set<WikipediaEntity> relatedEntities;

  protected ExpandedWikipediaArticle(String title) {
    super(title);
    this.relatedEntities = Sets.newHashSet();
  }

  protected ExpandedWikipediaArticle(String title, RangeMap<Calendar, String> period2content) {
    super(title, period2content);
    this.relatedEntities = Sets.newHashSet();
  }

  protected ExpandedWikipediaArticle(String title, RangeMap<Calendar, String> period2content,
          Set<WikipediaEntity> relatedEntities) {
    super(title, period2content);
    this.relatedEntities = relatedEntities;
  }

  public static ExpandedWikipediaArticle newPeriodicalArticle(String title, Calendar latest,
          Calendar earliest, Wiki wiki) throws IOException {
    ExpandedWikipediaArticle article = new ExpandedWikipediaArticle(title);
    Calendar endTime = latest;
    for (Revision revision : wiki.getPageHistoryWithInitialVersion(title, latest, earliest)) {
      article.addPeriodicContent(Range.closedOpen(revision.getTimestamp(), endTime),
              revision.getText());
      endTime = revision.getTimestamp();
    }
    return article;
  }

  public void addRelatedEntity(WikipediaEntity entity) {
    relatedEntities.add(entity);
  }

  public void addRelatedEntities(Collection<WikipediaEntity> entities) {
    relatedEntities.addAll(entities);
  }

  public void clearRelatedEntities() {
    relatedEntities.clear();
  }

  public Set<WikipediaEntity> getRelatedEntities(WikipediaEntity.Relation relation) {
    return Sets.filter(relatedEntities, new WikipediaEntity.RelationPredicate(relation));
  }

  public Set<WikipediaEntity> getRelatedEntities() {
    return relatedEntities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((relatedEntities == null) ? 0 : relatedEntities.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ExpandedWikipediaArticle other = (ExpandedWikipediaArticle) obj;
    if (relatedEntities == null) {
      if (other.relatedEntities != null)
        return false;
    } else if (!relatedEntities.equals(other.relatedEntities))
      return false;
    return true;
  }

  public String getSizeSummary() {
    return entity + "\t" + size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.CATEGORY).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.OUTLINK).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.REDIRECT).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.INLINK).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.OUTLINK_ANCHOR).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.INLINK_ANCHOR).size() + "\t"
            + getRelatedEntities(WikipediaEntity.Relation.BOLDTEXT).size() + "\t";
  }

}

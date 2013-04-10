package edu.cmu.cs.ziy.wiki.article;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import edu.cmu.cs.ziy.util.DefaultPeriodicallyChangedObject;
import edu.cmu.cs.ziy.util.GuavaUtils;
import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity;
import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity.Relation;

public class WikipediaArticle extends DefaultPeriodicallyChangedObject<String> implements
        Serializable {

  private static final long serialVersionUID = 1L;

  protected WikipediaEntity entity;

  protected WikipediaArticle(String title) {
    super();
    this.entity = WikipediaEntity.newInvalidInstance(title, Relation.ORIGINAL);
  }

  protected WikipediaArticle(String title, RangeMap<Calendar, String> period2content) {
    super(period2content);
    this.entity = WikipediaEntity.newInstance(title, Relation.ORIGINAL,
            GuavaUtils.keySet(period2content));
  }

  public static WikipediaArticle newPeriodicalArticle(String title, Calendar latest,
          Calendar earliest, Wiki wiki) throws IOException {
    WikipediaArticle article = new WikipediaArticle(title);
    Calendar endTime = latest;
    for (Revision revision : wiki.getPageHistoryWithInitialVersion(title, latest, earliest)) {
      // http://en.wikipedia.org/w/index.php?maxlag=5&title=Israel+and+the+apartheid+analogy&oldid=462866082&action=raw
      try {
        String text = revision.getText();
        article.addPeriodicContent(Range.closedOpen(revision.getTimestamp(), endTime), text);
      } catch (FileNotFoundException e) {
        System.err.println("Deleted revision:" + e.getMessage());
      }
      endTime = revision.getTimestamp();
    }
    return article;
  }

  public void addPeriodicContent(Range<Calendar> period, String content) {
    addValuePeriod(period, content);
    entity.addValidPeriod(period);
  }

  public WikipediaEntity getEntity() {
    return entity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
    WikipediaArticle other = (WikipediaArticle) obj;
    if (entity == null) {
      if (other.entity != null)
        return false;
    } else if (!entity.equals(other.entity))
      return false;
    return true;
  }

}

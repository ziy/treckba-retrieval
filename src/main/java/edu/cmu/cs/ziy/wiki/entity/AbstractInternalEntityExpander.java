package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;
import java.util.Map.Entry;

import org.wikipedia.Wiki;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import edu.cmu.cs.ziy.wiki.article.WikipediaArticle;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;

public abstract class AbstractInternalEntityExpander extends AbstractWikipediaEntityExpander
        implements WikipediaEntityExpander {

  @Override
  public abstract Set<WikipediaEntity> generateEntities(String originalEntity,
          Range<Calendar> period, Wiki wiki) throws IOException;

  @Override
  public RangeSet<Calendar> validateExistence(String originalEntity, String expandedEntity,
          Range<Calendar> period, Wiki wiki) throws IOException {
    RangeSet<Calendar> periods = TreeRangeSet.create();
    WikipediaArticle article = WikipediaArticleCache.loadArticle(originalEntity, period, wiki);
    for (Entry<Range<Calendar>, String> revision : article.getPeriodicContentPairs()) {
      if (containsLink(revision.getValue(), expandedEntity)) {
        periods.add(Range.closedOpen(revision.getKey().lowerEndpoint(), revision.getKey()
                .upperEndpoint()));
      }
    }
    return periods;
  }

}

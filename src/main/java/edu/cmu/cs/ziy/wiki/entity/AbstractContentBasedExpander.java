package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import edu.cmu.cs.ziy.wiki.article.WikipediaArticle;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;

public abstract class AbstractContentBasedExpander extends AbstractValidationFreeExpander {

  public abstract String getTextExtractionPattern();

  public abstract WikipediaEntity.Relation getEntityRelation();

  @Override
  public Set<WikipediaEntity> generateEntities(String originalEntity, Range<Calendar> period,
          Wiki wiki) throws IOException {
    Pattern pattern = Pattern.compile(getTextExtractionPattern());
    WikipediaArticle article = WikipediaArticleCache.loadArticle(originalEntity, period, wiki);
    Map<String, WikipediaEntity> anchor2entity = Maps.newHashMap();
    for (Entry<Range<Calendar>, String> periodContent : article.getPeriodicContentPairs()) {
      Matcher matcher = pattern.matcher(periodContent.getValue());
      while (matcher.find()) {
        String anchor = Objects.firstNonNull(matcher.group(1), originalEntity).trim();
        if (!anchor2entity.containsKey(anchor)) {
          anchor2entity
                  .put(anchor, WikipediaEntity.newInvalidInstance(anchor, getEntityRelation()));
        }
        anchor2entity.get(anchor).addValidPeriod(periodContent.getKey());
      }
    }
    return Sets.newHashSet(anchor2entity.values());
  }

}

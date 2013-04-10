package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;

import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import edu.cmu.cs.ziy.wiki.WikipediaNamespacePredicate;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticle;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;

public class InlinkAnchorTextExpander extends AbstractValidationFreeExpander implements
        WikipediaEntityExpander {

  private static String ANCHOR_FIND_PATTERN = "\\[{2}%s(?:\\|(.*?))?\\]{2}";

  @Override
  public Set<WikipediaEntity> generateEntities(String originalEntity, Range<Calendar> period,
          Wiki wiki) throws IOException {
    Pattern pattern = Pattern.compile(String.format(ANCHOR_FIND_PATTERN, originalEntity));
    Map<String, WikipediaEntity> anchor2entity = Maps.newHashMap();
    for (String inlink : Collections2.filter(
            Arrays.asList(wiki.whatLinksHere(originalEntity, Wiki.MAIN_NAMESPACE)),
            new WikipediaNamespacePredicate(wiki, Wiki.MAIN_NAMESPACE))) {
      WikipediaArticle article = WikipediaArticleCache.loadArticle(inlink, period, wiki);
      for (Entry<Range<Calendar>, String> periodContent : article.getPeriodicContentPairs()) {
        Matcher matcher = pattern.matcher(periodContent.getValue());
        while (matcher.find()) {
          String anchor = Objects.firstNonNull(matcher.group(1), originalEntity).trim();
          if (!anchor2entity.containsKey(anchor)) {
            anchor2entity.put(anchor, WikipediaEntity.newInvalidInstance(anchor,
                    WikipediaEntity.Relation.INLINK_ANCHOR));
          }
          anchor2entity.get(anchor).addValidPeriod(periodContent.getKey());
        }
      }
    }
    return Sets.newHashSet(anchor2entity.values());
  }

}

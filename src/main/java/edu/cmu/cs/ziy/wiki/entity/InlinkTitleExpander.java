package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;

import org.wikipedia.Wiki;

import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import edu.cmu.cs.ziy.wiki.WikipediaNamespacePredicate;

public class InlinkTitleExpander extends AbstractExternalEntityExpander implements
        WikipediaEntityExpander {

  @Override
  public Set<WikipediaEntity> generateEntities(String originalEntity, Range<Calendar> period,
          Wiki wiki) throws IOException {
    Set<WikipediaEntity> inlinks = Sets.newHashSet();
    for (String inlink : Collections2.filter(
            Arrays.asList(wiki.whatLinksHere(originalEntity, Wiki.MAIN_NAMESPACE)),
            new WikipediaNamespacePredicate(wiki, Wiki.MAIN_NAMESPACE))) {
      inlinks.add(WikipediaEntity.newInvalidInstance(inlink, WikipediaEntity.Relation.INLINK));
    }
    return inlinks;
  }

}

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

public class OutlinkTitleExpander extends AbstractInternalEntityExpander implements
        WikipediaEntityExpander {

  @Override
  public Set<WikipediaEntity> generateEntities(String originalEntity, Range<Calendar> period,
          Wiki wiki) throws IOException {
    Set<WikipediaEntity> outlinks = Sets.newHashSet();
    for (String outlink : Collections2.filter(Arrays.asList(wiki.getLinksOnPage(originalEntity)),
            new WikipediaNamespacePredicate(wiki, Wiki.MAIN_NAMESPACE))) {
      outlinks.add(WikipediaEntity.newInvalidInstance(outlink,
              WikipediaEntity.Relation.OUTLINK));
    }
    return outlinks;
  }

}

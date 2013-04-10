package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;

public abstract class AbstractWikipediaEntityExpander implements WikipediaEntityExpander {

  private static String WIKI_ENTITY_PATTERN = "\\[{2}%s(?:\\]{2}|\\|)";

  @Override
  public abstract Set<WikipediaEntity> generateEntities(String originalEntity,
          Range<Calendar> period, Wiki wiki) throws IOException;

  @Override
  public abstract RangeSet<Calendar> validateExistence(String originalEntity,
          String expandedEntity, Range<Calendar> period, Wiki wiki) throws IOException;

  @Override
  public Set<WikipediaEntity> generateAndValidateExistence(String originalEntity,
          Range<Calendar> period, Wiki wiki) throws IOException {
    Set<WikipediaEntity> entities = generateEntities(originalEntity, period, wiki);
    for (WikipediaEntity entity : entities) {
      RangeSet<Calendar> periods = validateExistence(originalEntity, entity.getText(), period, wiki);
      entity.addValidPeriods(periods);
    }
    // DONE Remove entity with zero period
    return Sets.newHashSet(Collections2.filter(entities, new Predicate<WikipediaEntity>() {

      @Override
      public boolean apply(WikipediaEntity input) {
        return !input.getValidPeriods().isEmpty();
      }
    }));
  }

  protected boolean containsLink(String contentText, String expandedEntity) {
    Pattern pattern = Pattern.compile(String.format(WIKI_ENTITY_PATTERN, Pattern.quote(expandedEntity)));
    return pattern.matcher(contentText).find();
  }

}

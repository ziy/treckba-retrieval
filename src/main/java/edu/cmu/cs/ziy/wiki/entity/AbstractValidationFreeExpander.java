package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import org.wikipedia.Wiki;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public abstract class AbstractValidationFreeExpander extends AbstractWikipediaEntityExpander {

  @Override
  public abstract Set<WikipediaEntity> generateEntities(String originalEntity,
          Range<Calendar> period, Wiki wiki) throws IOException;

  @Override
  public RangeSet<Calendar> validateExistence(String originalEntity, String expandedEntity,
          Range<Calendar> period, Wiki wiki) throws IOException {
    return TreeRangeSet.create();
  }

}

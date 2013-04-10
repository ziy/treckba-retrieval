package edu.cmu.cs.ziy.wiki.entity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import org.wikipedia.Wiki;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class CategoryNameExpander extends AbstractInternalEntityExpander implements
        WikipediaEntityExpander {

  @Override
  public Set<WikipediaEntity> generateEntities(String originalEntity, Range<Calendar> period,
          Wiki wiki) throws IOException {
    Set<WikipediaEntity> categories = Sets.newHashSet();
    for (String category : wiki.getCategories(originalEntity)) {
      categories.add(WikipediaEntity.newInvalidInstance(category,
              WikipediaEntity.Relation.CATEGORY));
    }
    return categories;
  }

}

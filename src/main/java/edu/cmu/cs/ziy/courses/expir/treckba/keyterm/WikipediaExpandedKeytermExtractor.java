package edu.cmu.cs.ziy.courses.expir.treckba.keyterm;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.guava.RangeSetParser;
import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity;
import edu.cmu.cs.ziy.wiki.entity.WikipediaEntity.Relation;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class WikipediaExpandedKeytermExtractor extends SimpleKeytermExtractor {

  private static final String EXPANDED_KEYTERM_PROPERTY = "treckba-retrieval.keyterm.expanded-keyterm";

  private SetMultimap<String, WikipediaEntity> keyterm2expands = HashMultimap.create();

  private Set<Relation> relations = Sets.newHashSet();

  private static final DateFormat df = new SimpleDateFormat(CalendarUtils.YMDH_FORMAT);

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String expandedKeyterm = Objects.firstNonNull(System.getProperty(EXPANDED_KEYTERM_PROPERTY),
            (String) context.getConfigParameterValue("expanded-keyterm"));
    try {
      for (String line : Resources.readLines(Resources.getResource(getClass(), expandedKeyterm),
              Charsets.UTF_8)) {
        String[] segs = line.split("\t");
        WikipediaEntity entity = WikipediaEntity.newInstance(segs[0], Relation.valueOf(segs[2]),
                RangeSetParser.parse(segs[3], RangeSetParser.calendarParser(df)));
        keyterm2expands.put(segs[1], entity);
      }
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    if ((Boolean) context.getConfigParameterValue("use-redirect")) {
      relations.add(Relation.REDIRECT);
    }
    if ((Boolean) context.getConfigParameterValue("use-category")) {
      relations.add(Relation.CATEGORY);
    }
    if ((Boolean) context.getConfigParameterValue("use-inlink")) {
      relations.add(Relation.INLINK);
    }
    if ((Boolean) context.getConfigParameterValue("use-outlink")) {
      relations.add(Relation.OUTLINK);
    }
    if ((Boolean) context.getConfigParameterValue("use-inlink-anchor")) {
      relations.add(Relation.INLINK_ANCHOR);
    }
    if ((Boolean) context.getConfigParameterValue("use-outlink-anchor")) {
      relations.add(Relation.OUTLINK_ANCHOR);
    }
    if ((Boolean) context.getConfigParameterValue("use-boldtext")) {
      relations.add(Relation.BOLDTEXT);
    }
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    if (question.equals("William_H._Gates,_Sr")) {
      question = "William_H._Gates,_Sr.";
    }
    List<Keyterm> keyterms = Lists.newArrayList();

    try {
      String title = question.replace('_', ' ');
      for (WikipediaEntity entity : keyterm2expands.get(title)) {
        if (!relations.contains(entity.getRelation())) {
          continue;
        }
        Keyterm keyterm = new Keyterm(extractKeyterm(entity.getText()));
        keyterm.setComponentId(CalendarUtils.rangeSetToString(entity.getValidPeriods(),
                CalendarUtils.YMDH_FORMAT));
        keyterms.add(keyterm);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return keyterms;
  }
}

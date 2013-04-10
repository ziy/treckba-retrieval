package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.guava.RangeSetParser;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class PeriodicallyValidQueryWriter extends SimpleQueryWriter {

  private SimpleDateFormat df;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    df = new SimpleDateFormat(CalendarUtils.YMDH_FORMAT);
  }

  @Override
  protected List<String> generateQuery(List<Keyterm> keyterms) {
    Map<Keyterm, RangeSet<Calendar>> keyterm2periods = Maps.newHashMap();
    for (Keyterm keyterm : keyterms) {
      RangeSet<Calendar> periods = RangeSetParser.parse(keyterm.getComponentId(),
              RangeSetParser.calendarParser(df));
      keyterm2periods.put(keyterm, periods);
    }
    List<Calendar> checkpoints = Lists.newArrayList();
    for (Keyterm keyterm : keyterms) {
      for (Range<Calendar> period : keyterm2periods.get(keyterm).asRanges()) {
        checkpoints.add(period.lowerEndpoint());
        checkpoints.add(period.upperEndpoint());
      }
    }
    Collections.sort(checkpoints);
    for (int i = 0; i < checkpoints.size() - 1; i++) {
      Range<Calendar> period = Range.closedOpen(checkpoints.get(i), checkpoints.get(i + 1));
      List<Keyterm> periodicallyValidKeyterms = Lists.newArrayList();
      for (Keyterm keyterm : keyterms) {
        if (keyterm2periods.get(keyterm).encloses(period)) {
          periodicallyValidKeyterms.add(keyterm);
        }
      }
      String query = Joiner.on(' ').join(
              Lists.transform(periodicallyValidKeyterms, new KeytermStringGetter(quote)));
      String periodString;
      try {
        periodString = CalendarUtils.rangeToString(period, CalendarUtils.YMDH_FORMAT);
        allQueries.add(query + "\t" + periodString);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return Lists.newArrayList();
  }
}

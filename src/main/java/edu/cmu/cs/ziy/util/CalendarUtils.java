package edu.cmu.cs.ziy.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class CalendarUtils {

  public static final Calendar BIG_BANG = getInstance(Long.MIN_VALUE);

  public static final Calendar BIG_RIP = getInstance(Long.MAX_VALUE);

  public static final Calendar PRESENT = Calendar.getInstance();

  public static final String YMDH_FORMAT = "yyyy-MM-dd-HH";

  public static final Calendar getInstance(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);
    return calendar;
  }

  public static final Calendar getGmtInstance(long millis) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    calendar.setTimeInMillis(millis);
    return calendar;
  }

  public static Calendar getInstance(String timeString, String dateFormatPattern)
          throws ParseException {
    Calendar calendar = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat(dateFormatPattern);
    calendar.setTime(df.parse(timeString));
    return calendar;
  }

  public static Calendar getGmtInstance(String timeString, String dateFormatPattern)
          throws ParseException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    DateFormat df = new SimpleDateFormat(dateFormatPattern);
    calendar.setTime(df.parse(timeString));
    return calendar;
  }

  public static String toString(Calendar calendar, String dateFormatPattern) throws ParseException {
    DateFormat df = new SimpleDateFormat(dateFormatPattern);
    return df.format(calendar.getTime());
  }

  public static String rangeToString(Range<Calendar> range, String dateFormatPattern)
          throws ParseException {
    Range<String> rangeStrings = Range.range(
            CalendarUtils.toString(range.lowerEndpoint(), dateFormatPattern),
            range.lowerBoundType(),
            CalendarUtils.toString(range.upperEndpoint(), dateFormatPattern),
            range.upperBoundType());
    return rangeStrings.toString();
  }

  private static class CalendarFormatter implements Function<Range<Calendar>, String> {

    private String dateFormatPattern;

    public CalendarFormatter(String dateFormatPattern) {
      this.dateFormatPattern = dateFormatPattern;
    }

    @Override
    public String apply(Range<Calendar> input) {
      try {
        return rangeToString(input, dateFormatPattern);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  public static String rangeCollectionToString(Collection<Range<Calendar>> periods,
          String dateFormatPattern) {
    return Joiner.on(", ").join(
            Collections2.transform(periods, new CalendarFormatter(dateFormatPattern)));
  }

  public static String rangeSetToString(RangeSet<Calendar> periods, String dateFormatPattern) {
    return rangeCollectionToString(periods.asRanges(), dateFormatPattern);
  }
}

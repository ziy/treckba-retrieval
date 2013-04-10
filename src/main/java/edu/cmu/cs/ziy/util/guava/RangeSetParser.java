package edu.cmu.cs.ziy.util.guava;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class RangeSetParser {

  public static void main(String[] args) {
    RangeSet<Integer> rangeSet = TreeRangeSet.create();
    rangeSet.add(Range.closed(1, 10));
    rangeSet.add(Range.closedOpen(11, 15));
    rangeSet.add(Range.open(15, 20));
    rangeSet.add(Range.openClosed(0, 0));
    rangeSet.remove(Range.open(5, 10));
    String rangeSteStr = rangeSet.toString();
    RangeSet<Integer> recovered = parse(rangeSteStr, integerParser());
    System.out.println(recovered.equals(rangeSet));
  }

  public static Pattern RANGE_PATTERN = Pattern.compile("([\\[\\(])(.*?)\u2025(.*?)([\\]\\)])");

  public static <C extends Comparable<C>> RangeSet<C> parse(String str,
          Function<String, C> itemParser) {
    RangeSet<C> rangeSet = TreeRangeSet.create();
    Matcher matcher = RANGE_PATTERN.matcher(str);
    while (matcher.find()) {
      String leftBound = matcher.group(1);
      C leftValue = itemParser.apply(matcher.group(2));
      C rightValue = itemParser.apply(matcher.group(3));
      String rightBound = matcher.group(4);
      if (leftBound.equals("[") && rightBound.equals("]")) {
        rangeSet.add(Range.closed(leftValue, rightValue));
      } else if (leftBound.equals("[") && rightBound.equals(")")) {
        rangeSet.add(Range.closedOpen(leftValue, rightValue));
      } else if (leftBound.equals("(") && rightBound.equals(")")) {
        rangeSet.add(Range.open(leftValue, rightValue));
      } else if (leftBound.equals("(") && rightBound.equals("]")) {
        rangeSet.add(Range.openClosed(leftValue, rightValue));
      }
    }
    return rangeSet;
  }

  public static Function<String, Integer> integerParser() {
    return new Function<String, Integer>() {

      @Override
      public Integer apply(String input) {
        return Integer.parseInt(input);
      }
    };
  }

  public static Function<String, Calendar> calendarParser(final DateFormat sdf) {
    return new Function<String, Calendar>() {

      @Override
      public Calendar apply(String input) {
        Calendar calendar = Calendar.getInstance();
        try {
          calendar.setTime(sdf.parse(input));
        } catch (ParseException e) {
          e.printStackTrace();
        }
        return calendar;
      }
    };
  }
}

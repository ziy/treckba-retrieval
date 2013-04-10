package edu.cmu.cs.ziy.util;

import java.util.Calendar;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public interface PeriodicallyValid {

  void addValidPeriod(Range<Calendar> period);
  
  void addValidPeriods(RangeSet<Calendar> periods);

  boolean isValidAt(Calendar time);
}

package edu.cmu.cs.ziy.util;

import java.util.Calendar;
import java.util.Map.Entry;

import com.google.common.collect.Range;

public interface PeriodicallyChanged<T> {

  void addValuePeriod(Range<Calendar> period, T value);

  T getValueAt(Calendar time);
  
  Iterable<Entry<Range<Calendar>, T>> getPeriodicContentPairs();

  int size();
}

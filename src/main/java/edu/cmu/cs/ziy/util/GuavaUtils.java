package edu.cmu.cs.ziy.util;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class GuavaUtils {

  public static <T extends Comparable<T>> RangeSet<T> keySet(RangeMap<T, ?> map) {
    RangeSet<T> set = TreeRangeSet.create();
    for (Range<T> key : map.asMapOfRanges().keySet()) {
      set.add(key);
    }
    return set;
  }
}

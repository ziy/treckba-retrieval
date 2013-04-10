package edu.cmu.cs.ziy.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Set;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class DefaultPeriodicallyValidObject implements PeriodicallyValid, Serializable {

  private static final long serialVersionUID = 1L;

  protected RangeSet<Calendar> periods;

  public DefaultPeriodicallyValidObject() {
    super();
    this.periods = TreeRangeSet.create();
  }

  public DefaultPeriodicallyValidObject(RangeSet<Calendar> periods) {
    super();
    this.periods = periods;
  }

  @Override
  public void addValidPeriod(Range<Calendar> period) {
    this.periods.add(period);
  }
  
  @Override
  public void addValidPeriods(RangeSet<Calendar> periods) {
    this.periods.addAll(periods);
  }

  @Override
  public boolean isValidAt(Calendar time) {
    return periods.contains(time);
  }
  
  public RangeSet<Calendar> getValidPeriods() {
    return periods;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    Set<Range<Calendar>> ranges = periods.asRanges();
    out.writeInt(ranges.size());
    for (Range<Calendar> range : ranges) {
      out.writeObject(range);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    periods = TreeRangeSet.create();
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      periods.add((Range<Calendar>) in.readObject());
    }
  }

  @Override
  public String toString() {
    return CalendarUtils.rangeSetToString(this.periods, CalendarUtils.YMDH_FORMAT);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((periods == null) ? 0 : periods.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DefaultPeriodicallyValidObject other = (DefaultPeriodicallyValidObject) obj;
    if (periods == null) {
      if (other.periods != null)
        return false;
    } else if (!periods.equals(other.periods))
      return false;
    return true;
  }

}

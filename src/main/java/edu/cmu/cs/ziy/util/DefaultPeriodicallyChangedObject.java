package edu.cmu.cs.ziy.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class DefaultPeriodicallyChangedObject<T extends Serializable> implements
        PeriodicallyChanged<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private RangeMap<Calendar, T> period2value;

  public DefaultPeriodicallyChangedObject() {
    super();
    this.period2value = TreeRangeMap.create();
  }

  public DefaultPeriodicallyChangedObject(RangeMap<Calendar, T> period2value) {
    super();
    this.period2value = period2value;
  }

  @Override
  public void addValuePeriod(Range<Calendar> period, T value) {
    period2value.put(period, value);
  }

  @Override
  public T getValueAt(Calendar time) {
    return period2value.get(time);
  }

  @Override
  public Set<Entry<Range<Calendar>, T>> getPeriodicContentPairs() {
    return period2value.asMapOfRanges().entrySet();
  }
  
  @Override
  public int size() {
    return period2value.asMapOfRanges().size();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    Map<Range<Calendar>, T> pairs = period2value.asMapOfRanges();
    out.writeInt(pairs.size());
    for (Entry<Range<Calendar>, T> pair : pairs.entrySet()) {
      out.writeObject(pair.getKey());
      out.writeObject(pair.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    period2value = TreeRangeMap.create();
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      Range<Calendar> key = (Range<Calendar>) in.readObject();
      T value = (T) in.readObject();
      period2value.put(key, value);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((period2value == null) ? 0 : period2value.hashCode());
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
    DefaultPeriodicallyChangedObject<?> other = (DefaultPeriodicallyChangedObject<?>) obj;
    if (period2value == null) {
      if (other.period2value != null)
        return false;
    } else if (!period2value.equals(other.period2value))
      return false;
    return true;
  }

}

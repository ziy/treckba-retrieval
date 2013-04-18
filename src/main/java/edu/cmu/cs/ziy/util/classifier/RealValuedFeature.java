package edu.cmu.cs.ziy.util.classifier;

public class RealValuedFeature extends Feature {

  private double value;

  public RealValuedFeature(double value) {
    super();
    this.value = value;
  }

  public RealValuedFeature(Object value) {
    this(Double.parseDouble(value.toString()));
  }

  public double getDoubleValue() {
    return value;
  }

  @Override
  public String getValue() {
    return String.valueOf(value);
  }

  @Override
  public Type getType() {
    return Type.REAL_VALUED;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(value);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    RealValuedFeature other = (RealValuedFeature) obj;
    if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
      return false;
    return true;
  }
  
}

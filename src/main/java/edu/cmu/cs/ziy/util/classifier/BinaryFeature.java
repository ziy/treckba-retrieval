package edu.cmu.cs.ziy.util.classifier;

public class BinaryFeature extends Feature {

  private boolean value;

  public BinaryFeature(boolean value) {
    super();
    this.value = value;
  }

  public boolean getBooleanValue() {
    return value;
  }

  @Override
  public String getValue() {
    return String.valueOf(value);
  }

  @Override
  public Type getType() {
    return Type.BINARY;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (value ? 1231 : 1237);
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
    BinaryFeature other = (BinaryFeature) obj;
    if (value != other.value)
      return false;
    return true;
  }

}

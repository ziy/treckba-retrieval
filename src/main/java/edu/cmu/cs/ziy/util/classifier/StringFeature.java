package edu.cmu.cs.ziy.util.classifier;

public class StringFeature extends Feature {

  private String value;
  
  public StringFeature(String value) {
    super();
    this.value = value;
  }

  public StringFeature(Object value) {
    this(value.toString());
  }
  
  public String getStringValue() {
    return value;
  }
  
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public Type getType() {
    return Type.STRING;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    StringFeature other = (StringFeature) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
  
}

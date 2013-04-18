package edu.cmu.cs.ziy.util.classifier;

public abstract class Feature {

  public static enum Type {
    STRING, REAL_VALUED, BINNED_VALUED, BINARY
  };

  public abstract Type getType();

  public abstract String getValue();

  @Override
  public String toString() {
    return getValue();
  }
}

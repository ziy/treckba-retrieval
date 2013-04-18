package edu.cmu.cs.ziy.util.classifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Instance {

  private static List<Set<Feature>> featureValues = new ArrayList<Set<Feature>>();

  private static Set<Object> labelValues = new HashSet<Object>();

  private static List<String> featureNames = new ArrayList<String>();

  private Object label;

  private List<Feature> features;

  public Instance(Object label, List<Feature> features) {
    this(features);
    setLabel(label);
  }

  public Instance(List<Feature> features) {
    this.features = features;
    while (featureValues.size() < features.size()) {
      featureValues.add(new HashSet<Feature>());
    }
    int i = 0;
    for (Feature feature : features) {
      featureValues.get(i++).add(feature);
    }
  }

  @Override
  public String toString() {
    return label + " <- " + features;
  }

  public List<String> getFeatureStrings() {
    List<String> strings = new ArrayList<String>();
    for (Feature feature : features) {
      strings.add(feature.getValue());
    }
    return strings;
  }

  public List<Feature> getFeatures() {
    return features;
  }

  public String getLabelString() {
    return label == null ? null : label.toString();
  }

  public void setLabel(Object label) {
    this.label = label;
    labelValues.add(label);
  }

  public Object getLabel() {
    return label;
  }

  public static Set<Feature> getFeatureValues(int idx) {
    return featureValues.get(idx);
  }

  public static Set<Object> getLabelValues() {
    return labelValues;
  }

  public static void setFeatureNames(List<String> featureNames) {
    Instance.featureNames = featureNames;
  }

  public static String getFeatureName(int idx) {
    return featureNames.get(idx);
  }
}

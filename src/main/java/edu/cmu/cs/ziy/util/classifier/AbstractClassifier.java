package edu.cmu.cs.ziy.util.classifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractClassifier {

  public abstract void train(Collection<Instance> instances);

  public abstract Map<? extends Object, Double> infer(Instance instance);

  public final Object predict(Instance instance) {
    Object maxLabel = null;
    double maxValue = Double.NEGATIVE_INFINITY;
    for (Map.Entry<? extends Object, Double> entry : infer(instance).entrySet()) {
      if (entry.getValue() > maxValue) {
        maxLabel = entry.getKey();
        maxValue = entry.getValue();
      }
    }
    return maxLabel;
  }

  public abstract void setProperty(String key, String value);

  public abstract void setProperty(String key);

  public abstract void setFeatureTypes(List<Feature> features);

  public abstract void saveModel(File modelFile) throws IOException;
  
  public abstract void loadModel(File modelFile) throws IOException;
}
package edu.cmu.cs.ziy.util.classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OneVsAllClassifier extends AbstractClassifier {

  protected Map<Object, AbstractClassifier> label2classifier;

  protected Class<? extends AbstractClassifier> classifierClass;

  public OneVsAllClassifier(Class<? extends AbstractClassifier> classifierClass) {
    this.label2classifier = new HashMap<Object, AbstractClassifier>();
    this.classifierClass = classifierClass;
  }

  private AbstractClassifier newClassifier() {
    AbstractClassifier ret = null;
    try {
      ret = classifierClass.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return ret;
  }

  @Override
  public void train(Collection<Instance> instances) {
    // count labels and create classifiers
    Set<Object> labels = new HashSet<Object>();
    for (Instance instance : instances) {
      Object label = instance.getLabel();
      if (labels.add(label)) {
        label2classifier.put(label, newClassifier());
      }
    }
    // train N classifiers one by one
    for (Object label : labels) {
      List<Instance> workingInsts = new ArrayList<Instance>();
      for (Instance instance : instances) {
        if (instance.getLabel().equals(label)) {
          workingInsts.add(instance);
        } else {
          workingInsts.add(new Instance("TEMP", instance.getFeatures()));
        }
      }
      label2classifier.get(label).train(workingInsts);
    }
  }

  @Override
  public Map<Object, Double> infer(Instance instance) {
    Map<Object, Double> label2prob = new HashMap<Object, Double>();
    for (Object label : label2classifier.keySet()) {
      label2prob.put(label, label2classifier.get(label).infer(instance).get(label));
    }
    double sum = 0.0;
    for (Object label : label2prob.keySet()) {
      sum += label2prob.get(label);
    }
    for (Object label : label2prob.keySet()) {
      label2prob.put(label, label2prob.get(label) / sum);
    }
    return label2prob;
  }

  @Override
  public void setProperty(String key, String value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setProperty(String key) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setFeatureTypes(List<Feature> features) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveModel(File modelFile) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void loadModel(File modelFile) throws IOException {
    // TODO Auto-generated method stub
    
  }

}

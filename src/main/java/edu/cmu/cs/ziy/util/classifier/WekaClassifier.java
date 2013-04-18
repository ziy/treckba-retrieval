package edu.cmu.cs.ziy.util.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class WekaClassifier extends AbstractClassifier {

  protected Classifier classifier;

  private List<String> options;

  private int numFeatures;

  private Instances trainSet;

  private String[] idx2label;

  public static weka.core.Instance toWekaInstance(Instance instance, Instances trainSet) {
    double[] att = new double[instance.getFeatures().size() + 1];
    int i = 0;
    for (Feature feature : instance.getFeatures()) {
      switch (feature.getType()) {
        case REAL_VALUED:
          att[i] = ((RealValuedFeature) feature).getDoubleValue();
          break;
        case STRING:
        case BINARY:
          att[i] = trainSet.attribute(i).indexOfValue(feature.getValue());
          break;
        case BINNED_VALUED:
          break;
      }
      i++;
    }
    if (instance.getLabelString() != null) {
      att[i] = trainSet.classAttribute().indexOfValue(instance.getLabelString());
    }
    return new weka.core.Instance(1.0, att);
  }

  public WekaClassifier(Class<? extends Classifier> classifier) {
    try {
      this.classifier = classifier.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    this.options = new ArrayList<String>();
  }

  @Override
  public void setProperty(String key, String value) {
    options.add("-" + key);
    options.add(value);
  }

  @Override
  public void setProperty(String key) {
    options.add("-" + key);
  }

  @Override
  public void setFeatureTypes(List<Feature> features) {
    // add features to attributes
    numFeatures = features.size();
    FastVector atts = new FastVector(numFeatures + 1);
    for (int i = 0; i < numFeatures; i++) {
      switch (features.get(i).getType()) {
        case REAL_VALUED:
          atts.addElement(new Attribute(i + ":" + Instance.getFeatureName(i)));
          break;
        case STRING:
        case BINARY:
          // convert string features to nominal features
          Set<Feature> values = Instance.getFeatureValues(i);
          FastVector nominals = new FastVector(values.size());
          for (Feature feature : values) {
            nominals.addElement(feature.getValue());
          }
          atts.addElement(new Attribute(i + ":" + Instance.getFeatureName(i), nominals));
          break;
        case BINNED_VALUED:
          break;
      }
    }
    // add label to the attributes
    Set<Object> labels = Instance.getLabelValues();
    FastVector nominals = new FastVector(labels.size());
    for (Object label : labels) {
      nominals.addElement(label.toString());
    }
    atts.addElement(new Attribute("label", nominals));
    // create training set container
    trainSet = new Instances("training", atts, 0);
    trainSet.setClassIndex(numFeatures);
  }

  @Override
  public void train(Collection<Instance> instances) {
    // load instances
    for (Instance instance : instances) {
      trainSet.add(toWekaInstance(instance, trainSet));
    }
    // train
    try {
      classifier.setOptions(options.toArray(new String[0]));
      classifier.buildClassifier(trainSet);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // map class idx to class labels
    Attribute att = trainSet.classAttribute();
    idx2label = new String[att.numValues()];
    for (int i = 0; i < att.numValues(); i++) {
      idx2label[i] = att.value(i);
    }
  }

  @Override
  public Map<String, Double> infer(Instance instance) {
    // create instance
    weka.core.Instance inst = toWekaInstance(instance, trainSet);
    inst.setDataset(trainSet);
    // predict
    Map<String, Double> ret = null;
    try {
      ret = new HashMap<String, Double>();
      double[] result = classifier.distributionForInstance(inst);
      for (int j = 0; j < result.length; j++) {
        ret.put(idx2label[j], result[j]);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  @Override
  public void saveModel(File modelFile) throws IOException {
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile));
    oos.writeObject(classifier);
    oos.writeObject(trainSet);
    oos.writeObject(idx2label);
    oos.flush();
    oos.close();
  }

  @Override
  public void loadModel(File modelFile) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile));
    try {
      classifier = (Classifier) ois.readObject();
      trainSet = (Instances) ois.readObject();
      idx2label = (String[]) ois.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    ois.close();
  }

}

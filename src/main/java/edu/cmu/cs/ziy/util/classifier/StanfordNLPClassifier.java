package edu.cmu.cs.ziy.util.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.stats.Counter;

public class StanfordNLPClassifier extends AbstractClassifier {

  private Classifier<String, String> classifier;

  private final Properties prop;

  private final GeneralDataset<String, String> dataset;

  public StanfordNLPClassifier(Properties prop) {
    this.prop = prop;
    this.dataset = new Dataset<String, String>();
  }

  public StanfordNLPClassifier() {
    this.prop = new Properties();
    this.dataset = new Dataset<String, String>();
    setProperty("useClassFeature");
    setProperty("useNB");
    setProperty("prior", "no");
  }

  @Override
  public void setProperty(String key, String value) {
    prop.setProperty(key, value);
  }

  @Override
  public void setProperty(String key) {
    prop.setProperty(key, "true");
  }

  @Override
  public void setFeatureTypes(List<Feature> features) {
    for (int i = 0; i < features.size(); i++) {
      setFeatureType(i + 1, features.get(i));
    }
  }

  private void setFeatureType(int idx, Feature feature) {
    String param = null;
    switch (feature.getType()) {
      case STRING:
      case BINARY:
        param = "useString";
        break;
      case REAL_VALUED:
        param = "realValued";
        break;
      case BINNED_VALUED:
        break;
    }
    setProperty(idx + "." + param, "true");
  }

  public void setTolerance(double tolerance) {
    prop.setProperty("tolerance", String.valueOf(tolerance)); // "1e-3"
  }

  public void setSigma(double sigma) {
    prop.setProperty("sigma", String.valueOf(sigma)); // "0.6"
  }

  @Override
  public void train(Collection<Instance> instances) {
    for (Instance instance : instances) {
      dataset.add(new BasicDatum<String, String>(instance.getFeatureStrings(), instance
              .getLabelString()));
    }
    setFeatureTypes(instances.toArray(new Instance[0])[0].getFeatures());
    ColumnDataClassifier cdc = new ColumnDataClassifier(prop);
    classifier = cdc.makeClassifier(dataset);
  }

  @Override
  public Map<String, Double> infer(Instance instance) {
    Counter<String> distr = classifier.scoresOf(new BasicDatum<String, String>(instance
            .getFeatureStrings(), instance.getLabelString()));
    Map<String, Double> result = new HashMap<String, Double>();
    double sum = 0.0;
    for (Map.Entry<String, Double> entry : distr.entrySet()) {
      double expValue = Math.exp(entry.getValue());
      sum += expValue;
      result.put(entry.getKey(), expValue);
    }
    for (Map.Entry<String, Double> entry : result.entrySet()) {
      result.put(entry.getKey(), entry.getValue() / sum);
    }
    return result;
  }

  @Override
  public void saveModel(File modelFile) throws IOException {
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile));
    oos.writeObject(classifier);
    oos.flush();
    oos.close();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void loadModel(File modelFile) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile));
    try {
      classifier = (Classifier<String, String>) ois.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    ois.close();
  }

}

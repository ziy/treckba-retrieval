package edu.cmu.cs.ziy.util.classifier;

import weka.classifiers.trees.J48;

public class DecisionTreeClassifier extends WekaClassifier {

  public DecisionTreeClassifier() {
    super(J48.class);
    setProperty("U");
    setProperty("A");
    // setProperty("L");
    // setProperty("C", "0.9");
  }

}

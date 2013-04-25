package edu.cmu.cs.ziy.util.classifier;

import weka.classifiers.functions.LibSVM;

public class LogisticClassifier extends WekaClassifier {

  public LogisticClassifier() {
    super(LibSVM.class);
  }

}

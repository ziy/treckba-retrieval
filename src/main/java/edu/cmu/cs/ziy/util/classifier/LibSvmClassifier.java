package edu.cmu.cs.ziy.util.classifier;

import weka.classifiers.functions.Logistic;

public class LibSvmClassifier extends WekaClassifier {

  public LibSvmClassifier() {
    super(Logistic.class);
  }

}

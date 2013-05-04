package edu.cmu.cs.ziy.util.classifier;

import weka.classifiers.trees.RandomForest;

public class RandomForestClassifier extends WekaClassifier {

  public RandomForestClassifier() {
    super(RandomForest.class);
  }

}

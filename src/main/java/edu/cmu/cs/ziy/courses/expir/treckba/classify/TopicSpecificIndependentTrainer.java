package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import util.SimilarityUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.cmu.cs.ziy.util.classifier.AbstractClassifier;
import edu.cmu.cs.ziy.util.classifier.Instance;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class TopicSpecificIndependentTrainer extends AbstractDocumentClassifierTrainer {

  protected List<Instance> allInstances = Lists.newArrayList();

  protected void trainRetrieval(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<String> gsIds) throws ClassNotFoundException,
          IOException, ParseException, InstantiationException, IllegalAccessException {
    // prepare source
    HashMap<String, Double> articleWordCount = getArticleWordCount(question, wikiCacheDir);
    // add positive instance
    List<Instance> instances = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      if (gsIds.contains(id) && id2text.containsKey(id)) {
        HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
        instances.add(new Instance(Boolean.TRUE, extractFeatures(articleWordCount, textWordCount,
                document.getProbability())));
      }
    }
    System.out.println("Pos: " + instances.size());
    // add negative instance after sampling
    int posSize = instances.size();
    float ratio = (float) posSize / (documents.size() - posSize);
    Set<String> retrievedIds = Sets.newHashSet();
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      retrievedIds.add(id);
      if (!gsIds.contains(id) && Math.random() < ratio && id2text.containsKey(id)) {
        HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
        instances.add(new Instance(Boolean.FALSE, extractFeatures(articleWordCount, textWordCount,
                document.getProbability())));
      }
    }
    if (backgroundNegative) {
      List<String> idPool = Lists.newArrayList(Sets.difference(
              Sets.difference(id2text.keySet(), Sets.newHashSet(gsIds)), retrievedIds));
      Random random = new Random();
      for (int i = 0; i < posSize; i++) {
        String id = idPool.get(random.nextInt(idPool.size()));
        HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
        instances.add(new Instance(Boolean.FALSE, extractFeatures(articleWordCount, textWordCount,
                0)));
      }
    }
    System.out.println("Total: " + instances.size());
    allInstances.addAll(instances);
    // train
    AbstractClassifier classifier = clazz.newInstance();
    for (Entry<String, String> entry : props.entrySet()) {
      classifier.setProperty(entry.getKey(), entry.getValue());
    }
    classifier.setFeatureTypes(instances.get(0).getFeatures());
    classifier.train(instances);
    classifier.saveModel(new File(classifierModelDir, question + ".model"));
    // test
    for (RetrievalResult document : documents) {
      updateDocument(document, id2text, articleWordCount, classifier);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    // train
    AbstractClassifier classifier = null;
    try {
      classifier = clazz.newInstance();
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
    for (Entry<String, String> entry : props.entrySet()) {
      classifier.setProperty(entry.getKey(), entry.getValue());
    }
    classifier.setFeatureTypes(allInstances.get(0).getFeatures());
    classifier.train(allInstances);
    try {
      classifier.saveModel(new File(classifierModelDir, "_.model"));
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
    // test
    // TODO cannot test
  }
}
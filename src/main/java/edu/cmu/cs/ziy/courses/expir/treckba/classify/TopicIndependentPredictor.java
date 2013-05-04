package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.Lists;

import edu.cmu.cs.ziy.util.classifier.AbstractClassifier;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class TopicIndependentPredictor extends AbstractDocumentClassifierPredictor {

  protected AbstractClassifier classifier;
  
  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      classifier = clazz.newInstance();
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
    for (Entry<String, String> entry : props.entrySet()) {
      classifier.setProperty(entry.getKey(), entry.getValue());
    }
    try {
      classifier.loadModel(new File(classifierModelDir, "_.model"));
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    
  }
  
  protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    // prepare source
    HashMap<String, Double> articleWordCount = null;
    try {
      articleWordCount = getArticleWordCount(question, wikiCacheDir);
    } catch (Exception e) {
      return Lists.newArrayList();
    }
    // test
    for (RetrievalResult document : documents) {
      updateDocument(document, id2text, articleWordCount, classifier);
    }
    return documents;
  }
}
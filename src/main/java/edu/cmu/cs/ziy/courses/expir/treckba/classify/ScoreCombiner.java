package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.util.Collections;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class ScoreCombiner extends AbstractRetrievalUpdater {

  private static final String WEIGHT_PROPERTY = "treckba-retrieval.classifier.weight";

  private float weight;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (System.getProperty(WEIGHT_PROPERTY) != null) {
      weight = Float.parseFloat(System.getProperty(WEIGHT_PROPERTY));
    } else {
      weight = (Float) context.getConfigParameterValue("weight");
    }
  }

  @Override
  protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<Double> classifyScores = Lists.newArrayList();
    List<Double> retrievalScores = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      document.setProbablity(document.getProbability() * weight
              + Float.valueOf(document.getComponentId()) * (1.0f - weight));
      classifyScores.add((double) document.getProbability());
      retrievalScores.add((double) Float.valueOf(document.getComponentId()));
    }
    Collections.sort(classifyScores);
    int size = classifyScores.size();
    System.out.println("classify: " + size + " " + classifyScores.get(0) + " "
            + classifyScores.get(size / 4) + " " + classifyScores.get(size / 2) + " "
            + classifyScores.get(size * 3 / 4) + " " + classifyScores.get(size - 1));
    Collections.sort(retrievalScores);
    size = retrievalScores.size();
    System.out.println("retrieval: " + size + " " + retrievalScores.get(0) + " "
            + retrievalScores.get(size / 4) + " " + retrievalScores.get(size / 2) + " "
            + retrievalScores.get(size * 3 / 4) + " " + retrievalScores.get(size - 1));
    return documents;
  }
}

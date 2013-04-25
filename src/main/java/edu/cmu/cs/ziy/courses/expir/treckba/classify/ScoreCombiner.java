package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

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
    for (RetrievalResult document : documents) {
      document.setProbablity(document.getProbability() * weight
              + Float.valueOf(document.getComponentId()) * (1.0f - weight));
    }
    return documents;
  }
}

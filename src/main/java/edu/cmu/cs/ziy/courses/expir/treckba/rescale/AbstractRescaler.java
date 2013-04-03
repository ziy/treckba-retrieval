package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public abstract class AbstractRescaler extends AbstractRetrievalUpdater {

  protected abstract List<RetrievalResult> rescale(List<RetrievalResult> documents);

  @Override
  protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    return rescale(documents);
  }

  protected float bound(float f, float min, float max) {
    return Math.min(Math.max(f, min), max);
  }

}

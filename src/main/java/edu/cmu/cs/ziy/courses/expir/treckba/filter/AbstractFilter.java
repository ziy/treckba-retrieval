package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public abstract class AbstractFilter extends AbstractRetrievalUpdater {

  protected abstract List<RetrievalResult> filter(List<RetrievalResult> documents);

  @Override
  protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    return filter(documents);
  }

}

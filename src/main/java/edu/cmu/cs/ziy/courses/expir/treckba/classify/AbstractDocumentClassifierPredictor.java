package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public abstract class AbstractDocumentClassifierPredictor extends AbstractTrainerPredictor {

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // prepare input
      InputElement input = ((InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type));
      List<Keyterm> keyterms = KeytermList.retrieveKeyterms(jcas);
      List<RetrievalResult> documents = RetrievalResultArray.retrieveRetrievalResults(ViewManager
              .getDocumentView(jcas));
      // do task
      documents = updateDocuments(input.getQuestion(), keyterms, documents);
      // save output
      RetrievalResultArray.storeRetrievalResults(ViewManager.getDocumentView(jcas), documents);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected abstract List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents);

}

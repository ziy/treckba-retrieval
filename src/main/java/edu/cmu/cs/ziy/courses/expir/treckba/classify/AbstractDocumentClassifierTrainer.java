package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public abstract class AbstractDocumentClassifierTrainer extends AbstractTrainerPredictor {

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // prepare input
      InputElement input = ((InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type));
      List<Keyterm> keyterms = KeytermList.retrieveKeyterms(jcas);
      List<RetrievalResult> documents = RetrievalResultArray.retrieveRetrievalResults(ViewManager
              .getDocumentView(jcas));
      // prepare gs
      List<String> gsIds = Lists.newArrayList();
      for (RetrievalResult gs : RetrievalResultArray.retrieveRetrievalResults(ViewManager.getView(
              jcas, TrecKbaViewType.DOCUMENT_GS_RELEVANT))) {
        gsIds.add(gs.getDocID());
      }
      // do task
      trainRetrieval(input.getQuestion(), keyterms, documents, gsIds);
      // save output
      RetrievalResultArray.storeRetrievalResults(ViewManager.getDocumentView(jcas), documents);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected abstract void trainRetrieval(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<String> gsIds) throws ClassNotFoundException,
          IOException, ParseException, InstantiationException, IllegalAccessException;
}

package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class ThresholdTrainer extends AbstractThresholdTrainerPredictor {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // prepare input
      InputElement input = ((InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type));
      List<Keyterm> keyterms = KeytermList.retrieveKeyterms(jcas);
      List<RetrievalResult> documents = RetrievalResultArray.retrieveRetrievalResults(ViewManager
              .getDocumentView(jcas));
      // prepare gs
      List<String> gsIds = Lists.newArrayList();
      JCas view = null;
      if (gsRelevance.equals(GsRelevance.RELEVANT)) {
        view = ViewManager.getView(jcas, TrecKbaViewType.DOCUMENT_GS_RELEVANT);
      } else if (gsRelevance.equals(GsRelevance.CENTRAL)) {
        view = ViewManager.getView(jcas, TrecKbaViewType.DOCUMENT_GS_CENTRAL);
      }
      for (RetrievalResult gs : RetrievalResultArray.retrieveRetrievalResults(view)) {
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

  private void trainRetrieval(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<String> gsIds) throws IOException {
    List<Float> retrievalScores = Lists.newArrayList();
    List<Float> classifyScores = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      retrievalScores.add(Float.parseFloat(document.getComponentId()));
      classifyScores.add(document.getProbability());
    }
    Collections.sort(retrievalScores, Collections.reverseOrder());
    Collections.sort(classifyScores, Collections.reverseOrder());
    int size = Math.min(gsIds.size(), documents.size()) - 1;
    float retrievalThreshold = retrievalScores.get(size);
    float classifyThreshold = classifyScores.get(size);
    Files.write(retrievalThreshold + "\n" + classifyThreshold + "\n", new File(thresholdDir,
            question + ".threshold"), Charsets.UTF_8);
  }
}

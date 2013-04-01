package edu.cmu.cs.ziy.courses.expir.treckba.eval;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.eval.retrieval.RetrievalEvalConsumer;

public class RetrievalTraceEvaluatorAggregator extends RetrievalEvalConsumer<RetrievalResult> {

  private static final String RELEVANCE_LEVEL_PROPERTY = "treckba-retrieval.eval.relevance-level";

  private String relevanceLevel;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    relevanceLevel = Objects.firstNonNull(System.getProperty(RELEVANCE_LEVEL_PROPERTY),
            (String) context.getConfigParameterValue("relevance-level"));
  }

  @Override
  protected Ordering<RetrievalResult> getOrdering() {
    return new Ordering<RetrievalResult>() {

      @Override
      public int compare(RetrievalResult left, RetrievalResult right) {
        return left.getDocID().compareTo(right.getDocID());
      }

    }.reverse();
  }

  @Override
  protected Function<RetrievalResult, String> getToIdStringFct() {
    return new Function<RetrievalResult, String>() {

      @Override
      public String apply(RetrievalResult input) {
        return input.getDocID();
      }
    };
  }

  @Override
  protected List<RetrievalResult> getGoldStandard(JCas jcas) throws CASException {
    if (relevanceLevel.equalsIgnoreCase("RELEVANCE")) {
      try {
        return RetrievalResultArray.retrieveRetrievalResults(ViewManager.getOrCreateView(jcas,
                TrecKbaViewType.DOCUMENT_GS_RELEVANT));
      } catch (Exception e) {
        throw new CASException(e);
      }
    } else if (relevanceLevel.equalsIgnoreCase("CENTRAL")) {
      try {
        return RetrievalResultArray.retrieveRetrievalResults(ViewManager.getOrCreateView(jcas,
                TrecKbaViewType.DOCUMENT_GS_CENTRAL));
      } catch (Exception e) {
        throw new CASException(e);
      }
    } else {
      return Lists.newArrayList();
    }
  }

  @Override
  protected List<RetrievalResult> getResults(JCas jcas) throws CASException {
    try {
      return RetrievalResultArray.retrieveRetrievalResults(ViewManager.getOrCreateView(jcas,
              ViewType.DOCUMENT));
    } catch (Exception e) {
      throw new CASException(e);
    }
  }

}

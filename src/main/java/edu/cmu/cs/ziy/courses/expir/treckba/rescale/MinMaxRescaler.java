package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class MinMaxRescaler extends AbstractRescaler {

  private static final String MIN_PROPERTY = "treckba-retrieval.rescale.min";

  private static final String MAX_PROPERTY = "treckba-retrieval.rescale.max";

  private float min;

  private float max;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    min = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(MIN_PROPERTY))),
            (Float) context.getConfigParameterValue("min"));
    max = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(MAX_PROPERTY))),
            (Float) context.getConfigParameterValue("max"));
  }

  @Override
  protected List<RetrievalResult> rescale(List<RetrievalResult> documents) {
    float lowerbound = Float.POSITIVE_INFINITY;
    float upperbound = Float.NEGATIVE_INFINITY;
    for (RetrievalResult document : documents) {
      float score = document.getProbability();
      if (score < lowerbound) {
        lowerbound = score;
      }
      if (score > upperbound) {
        upperbound = score;
      }
    }
    float rescale = (max - min) / (upperbound - lowerbound);
    List<RetrievalResult> newDocuments = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      float score = bound((document.getProbability() - lowerbound) * rescale + min, min, max);
      newDocuments.add(new RetrievalResult(document.getDocID(), score, document.getQueryString()));
    }
    log("RESCALE: " + rescale + ", MIN: " + lowerbound + ", MAX: " + upperbound);
    return newDocuments;
  }

}

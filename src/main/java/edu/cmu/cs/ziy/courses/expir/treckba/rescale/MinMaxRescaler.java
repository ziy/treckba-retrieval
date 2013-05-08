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

  private static final String Y_MIN_PROPERTY = "treckba-retrieval.rescale.y-min";

  private static final String Y_MAX_PROPERTY = "treckba-retrieval.rescale.y-max";

  private float yMin;

  private float yMax;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    yMin = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(Y_MIN_PROPERTY))),
            (Float) context.getConfigParameterValue("y-min"));
    yMax = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(Y_MAX_PROPERTY))),
            (Float) context.getConfigParameterValue("y-max"));
  }

  @Override
  protected List<RetrievalResult> rescale(List<RetrievalResult> documents) {
    float xMin = Float.POSITIVE_INFINITY;
    float xMax = Float.NEGATIVE_INFINITY;
    for (RetrievalResult document : documents) {
      float score = document.getProbability();
      if (score < xMin) {
        xMin = score;
      }
      if (score > xMax) {
        xMax = score;
      }
    }
    float rescale = (yMax - yMin) / (xMax - xMin);
    List<RetrievalResult> newDocuments = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      float score = bound((document.getProbability() - xMin) * rescale + yMin, yMin, yMax);
      newDocuments.add(new RetrievalResult(document.getDocID(), score, document.getQueryString()));
    }
    log("RESCALE: " + rescale + ", x-min: " + xMin + ", x-max: " + xMax);
    return newDocuments;
  }

}

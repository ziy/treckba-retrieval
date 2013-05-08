package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class ConstantRescaler extends AbstractRescaler {

  private static final String X_MIN_PROPERTY = "treckba-retrieval.rescale.x-min";

  private static final String X_MAX_PROPERTY = "treckba-retrieval.rescale.x-max";

  private static final String Y_MIN_PROPERTY = "treckba-retrieval.rescale.y-min";

  private static final String Y_MAX_PROPERTY = "treckba-retrieval.rescale.y-max";

  private float xMin;

  private float xMax;

  private float yMin;

  private float yMax;

  private float rescale;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    xMin = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(X_MIN_PROPERTY))),
            (Float) context.getConfigParameterValue("x-min"));
    xMax = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(X_MAX_PROPERTY))),
            (Float) context.getConfigParameterValue("x-max"));
    yMin = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(Y_MIN_PROPERTY))),
            (Float) context.getConfigParameterValue("y-min"));
    yMax = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(Y_MAX_PROPERTY))),
            (Float) context.getConfigParameterValue("y-max"));
    rescale = (yMax - yMin) / (xMax - xMin);
    log("RESCALE: " + rescale + ", x-min: " + xMin + ", x-max: " + xMax + ", y-min: " + yMin
            + ", y-max: " + yMax);
  }

  @Override
  protected List<RetrievalResult> rescale(List<RetrievalResult> documents) {
    List<RetrievalResult> newDocuments = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      float score = bound((document.getProbability() - xMin) * rescale + yMin, yMin, yMax);
      newDocuments.add(new RetrievalResult(document.getDocID(), score, document.getQueryString()));
    }
    return newDocuments;
  }

}

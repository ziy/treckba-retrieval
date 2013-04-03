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

  private static final String LOWERBOUND_PROPERTY = "treckba-retrieval.rescale.lowerbound";

  private static final String UPPERBOUND_PROPERTY = "treckba-retrieval.rescale.upperbound";

  private static final String MIN_PROPERTY = "treckba-retrieval.rescale.min";

  private static final String MAX_PROPERTY = "treckba-retrieval.rescale.max";

  private float lowerbound;

  private float upperbound;

  private float min;

  private float max;

  private float rescale;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    lowerbound = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(LOWERBOUND_PROPERTY))),
            (Float) context.getConfigParameterValue("lowerbound"));
    upperbound = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(UPPERBOUND_PROPERTY))),
            (Float) context.getConfigParameterValue("upperbound"));
    min = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(MIN_PROPERTY))),
            (Float) context.getConfigParameterValue("min"));
    max = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(MAX_PROPERTY))),
            (Float) context.getConfigParameterValue("max"));
    rescale = (max - min) / (upperbound - lowerbound);
    log("RESCALE: " + rescale + ", LOWERBOUND: " + lowerbound + ", UPPERBOUND: " + upperbound
            + ", MIN: " + min + ", MAX: " + max);
  }

  @Override
  protected List<RetrievalResult> rescale(List<RetrievalResult> documents) {
    List<RetrievalResult> newDocuments = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      float score = bound((document.getProbability() - lowerbound) * rescale + min, min, max);
      newDocuments.add(new RetrievalResult(document.getDocID(), score, document.getQueryString()));
    }
    return newDocuments;
  }

}

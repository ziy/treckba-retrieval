package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class ThresholdingFilter extends AbstractFilter {

  private static final String THRESHOLD_PROPERTY = "treckba-retrieval.filter.threshold";

  private float threshold;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    threshold = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(THRESHOLD_PROPERTY))),
            (Float) context.getConfigParameterValue("threshold"));
  }

  @Override
  protected List<RetrievalResult> filter(List<RetrievalResult> documents) {
    List<RetrievalResult> newDocuments = Lists.newArrayList();
    for (RetrievalResult document : documents) {
      if (document.getProbability() >= threshold) {
        newDocuments.add(document);
      }
    }
    return newDocuments;
  }

}

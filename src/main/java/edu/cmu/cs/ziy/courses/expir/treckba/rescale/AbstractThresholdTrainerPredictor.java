package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;

import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.QALogEntry;

public class AbstractThresholdTrainerPredictor extends AbstractLoggedComponent {

  private static final String DIR_PROPERTY = "treckba-retrieval.rescale.dir";

  private static final String GS_RELEVANCE_PROPERTY = "treckba-retrieval.rescale.gs-relevance";

  protected static enum GsRelevance {
    CENTRAL, RELEVANT
  }

  protected GsRelevance gsRelevance;

  protected File thresholdDir;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    gsRelevance = GsRelevance.valueOf(Objects.firstNonNull(
            System.getProperty(GS_RELEVANCE_PROPERTY),
            (String) context.getConfigParameterValue("gs-relevance")));
    thresholdDir = new File(Objects.firstNonNull(System.getProperty(DIR_PROPERTY),
            (String) context.getConfigParameterValue("dir")));
    if (!thresholdDir.exists()) {
      thresholdDir.mkdir();
    }
  }

  protected void log(String message) {
    log(QALogEntry.RETRIEVAL, message);
  }
}

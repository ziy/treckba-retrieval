package edu.cmu.cs.ziy.courses.expir.treckba.rescale;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.primitives.Floats;

import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class ThresholdCurveRescaling extends AbstractThresholdTrainerPredictor {

  private static final String X_MIN_PROPERTY = "treckba-retrieval.rescale.x-min";

  private static final String X_MAX_PROPERTY = "treckba-retrieval.rescale.x-max";

  private static final String Y_MIN_PROPERTY = "treckba-retrieval.rescale.y-min";

  private static final String Y_MAX_PROPERTY = "treckba-retrieval.rescale.y-max";

  private static final String Y_THRESHOLD_PROPERTY = "treckba-retrieval.rescale.y-threshold";

  private static final String WEIGHT_PROPERTY = "treckba-retrieval.rescale.weight";

  private float xMin;

  private float xMax;

  private float yMin;

  private float yMax;

  private float yThreshold;

  private float weight;

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
    yThreshold = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(Y_THRESHOLD_PROPERTY))),
            (Float) context.getConfigParameterValue("y-threshold"));
    weight = Objects.firstNonNull(
            Floats.tryParse(Strings.nullToEmpty(System.getProperty(WEIGHT_PROPERTY))),
            (Float) context.getConfigParameterValue("weight"));
  }

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

  private List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) throws IOException {
    List<String> lines = Files.readLines(new File(thresholdDir, question + ".threshold"),
            Charsets.UTF_8);
    float retrievalThreshold = Float.valueOf(lines.get(0));
    float classifyThreshold = Float.valueOf(lines.get(1));
    double x0 = classifyThreshold * weight + retrievalThreshold * (1.0 - weight);
    // calculate power and rescale
    double logXMaxDiff = Math.log(xMax - xMin);
    double logX0Diff = Math.log(x0 - xMin);
    double logYMaxDiff = Math.log(yMax - yMin);
    double logY0Diff = Math.log(yThreshold - yMin);
    double power = ((logYMaxDiff - logY0Diff) / (logXMaxDiff - logX0Diff));
    double rescale = Math.exp((logXMaxDiff * logY0Diff - logX0Diff * logYMaxDiff)
            / (logXMaxDiff - logX0Diff));
    log("THRESHOLD: " + rescale + " POWER: " + power + ", x-min: " + xMin + ", x-max: " + xMax
            + ", y-min: " + yMin + ", y-max: " + yMax);
    // update documents
    for (RetrievalResult document : documents) {
      document.setProbablity((float) rescale(document.getProbability(), power, rescale));
    }
    return documents;
  }

  private double rescale(double origScore, double power, double rescale) {
    double newScore = rescale * Math.pow(origScore - xMin, power) + yMin;
    if (newScore < yMin) {
      newScore = yMin;
    }
    if (newScore > yMax) {
      newScore = yMax;
    }
    return newScore;
  }
}

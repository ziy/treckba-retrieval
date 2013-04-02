package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.lti.oaqa.framework.CasUtils;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class RetrievalGoldStandardDecorator extends JCasAnnotator_ImplBase {

  private static final String GSPATH_PROPERTY = "treckba-retrieval.collection.gspath";

  private ListMultimap<String, String> topic2relevant = ArrayListMultimap.create();

  private ListMultimap<String, String> topic2central = ArrayListMultimap.create();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String gsPath = Objects.firstNonNull(System.getProperty(GSPATH_PROPERTY),
            (String) context.getConfigParameterValue("gspath"));
    List<String> lines;
    try {
      lines = Resources.readLines(Resources.getResource(getClass(), gsPath),
              Charset.defaultCharset());
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    for (String line : Collections2.filter(lines, Predicates.containsPattern("^[^#]"))) {
      String[] segs = line.split("\t");
      String topic = segs[3];
      String streamId = segs[2];
      String relevanceLevel = segs[5];
      if (relevanceLevel.equals("1")) {
        topic2relevant.put(topic, streamId);
      } else if (relevanceLevel.equals("2")) {
        topic2relevant.put(topic, streamId);
        topic2central.put(topic, streamId);
      }
    }
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    JCas gsRelevantView;
    JCas gsCentralView;
    try {
      gsRelevantView = ViewManager.getOrCreateView(jcas, TrecKbaViewType.DOCUMENT_GS_RELEVANT);
      gsCentralView = ViewManager.getOrCreateView(jcas, TrecKbaViewType.DOCUMENT_GS_CENTRAL);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
    InputElement input = (InputElement) CasUtils.getFirst(jcas, InputElement.class.getName());
    String topic = input.getQuestion();
    List<RetrievalResult> relevantResults = Lists.newArrayList(Collections2.transform(
            topic2relevant.get(topic), new RetrievalResultMapper(topic)));
    List<RetrievalResult> centralResults = Lists.newArrayList(Collections2.transform(
            topic2central.get(topic), new RetrievalResultMapper(topic)));
    try {
      RetrievalResultArray.storeRetrievalResults(gsRelevantView, relevantResults);
      RetrievalResultArray.storeRetrievalResults(gsCentralView, centralResults);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  public static class RetrievalResultMapper implements Function<String, RetrievalResult> {

    private String topic;

    public RetrievalResultMapper(String topic) {
      this.topic = topic;
    }

    @Override
    public RetrievalResult apply(String streamId) {
      return new RetrievalResult(streamId, 1000.0f, topic);
    }

  }

}

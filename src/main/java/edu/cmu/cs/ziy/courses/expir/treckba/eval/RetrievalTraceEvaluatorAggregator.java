package edu.cmu.cs.ziy.courses.expir.treckba.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.eval.retrieval.RetrievalEvalConsumer;
import edu.cmu.lti.oaqa.framework.types.OutputElement;

public class RetrievalTraceEvaluatorAggregator extends RetrievalEvalConsumer<OutputElement> {

  private static final String RELEVANCE_LEVEL_PROPERTY = "treckba-retrieval.eval.relevance-level";

  private String relevanceLevel;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    relevanceLevel = Objects.firstNonNull(System.getProperty(RELEVANCE_LEVEL_PROPERTY),
            (String) context.getConfigParameterValue("relevance-level"));
  }

  @Override
  protected Ordering<OutputElement> getOrdering() {
    return new Ordering<OutputElement>() {

      @Override
      public int compare(OutputElement left, OutputElement right) {
        return left.getAnswer().compareTo(right.getAnswer());
      }

    }.reverse();
  }

  @Override
  protected Function<OutputElement, String> getToIdStringFct() {
    return new Function<OutputElement, String>() {

      @Override
      public String apply(OutputElement input) {
        return input.getAnswer();
      }
    };
  }

  @Override
  protected List<OutputElement> getGoldStandard(JCas jcas) throws CASException {
    if (relevanceLevel.equalsIgnoreCase("RELEVANCE")) {
      return getAnnotations(
              ViewManager.getOrCreateView(jcas, TrecKbaViewType.DOCUMENT_GS_RELEVANT),
              OutputElement.type);
    } else if (relevanceLevel.equalsIgnoreCase("CENTRAL")) {
      return getAnnotations(ViewManager.getOrCreateView(jcas, TrecKbaViewType.DOCUMENT_GS_CENTRAL),
              OutputElement.type);
    } else {
      return Lists.newArrayList();
    }
  }

  @Override
  protected List<OutputElement> getResults(JCas jcas) throws CASException {
    return getAnnotations(ViewManager.getOrCreateView(jcas, ViewType.DOCUMENT), OutputElement.type);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Annotation> List<T> getAnnotations(JCas jcas, int type) {
    List<T> annotations = new ArrayList<T>();
    for (Annotation annotation : jcas.getAnnotationIndex(type)) {
      if (annotation.getTypeIndexID() != type) {
        continue;
      }
      annotations.add((T) annotation);
    }
    return annotations;
  }

}

package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import similarity.AverageKLDivergence;
import similarity.CosineSimilarity;
import similarity.EuclideanDistance;
import similarity.JaccardCoefficient;
import similarity.KLDivergence;
import similarity.MatchingCoefficient;
import similarity.ModifiedOverlapCoefficient;
import similarity.PearsonCorrelationCoefficient;
import similarity.Similarity;
import util.SimilarityUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.classifier.Feature;
import edu.cmu.cs.ziy.util.classifier.Instance;
import edu.cmu.cs.ziy.util.classifier.RealValuedFeature;
import edu.cmu.cs.ziy.util.classifier.StanfordNLPClassifier;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.RetrievalResultArray;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class TopicSpecificClassifier {

  private static final String MODEL_DIR_PROPERTY = "treckba-retrieval.classifier.model-dir";

  private static final String WIKI_CACHE_DIR_PROPERTY = "treckba-retrieval.classifier.wiki-cache-dir";

  private static final String ID_TO_TEXT_FILE_PROPERTY = "treckba-retrieval.classifier.id-to-text-file";

  private static final String earliestTimeStr = "2011-10-07-14";

  private static final String latestTimeStr = "2012-05-02-00";

  private static final String criticalTimeStr = "2012-01-01-00";

  private static Range<Calendar> period;

  private static Calendar criticalTime;

  static {
    try {
      period = Range.closedOpen(
              CalendarUtils.getGmtInstance(earliestTimeStr, CalendarUtils.YMDH_FORMAT),
              CalendarUtils.getGmtInstance(latestTimeStr, CalendarUtils.YMDH_FORMAT));
      criticalTime = CalendarUtils.getGmtInstance(criticalTimeStr, CalendarUtils.YMDH_FORMAT);
    } catch (Exception e) {
    }
  }

  private static final Similarity[] sims = new Similarity[] { new CosineSimilarity(),
      new KLDivergence(), new JaccardCoefficient(), new PearsonCorrelationCoefficient(),
      new EuclideanDistance(EuclideanDistance.INVERSE_OF_DISTANCE_PLUS_ONE),
      new EuclideanDistance(EuclideanDistance.EXPONENTIAL_OF_NEGATIVE_DISTANCE),
      new MatchingCoefficient(), new ModifiedOverlapCoefficient(), new AverageKLDivergence() };

  public static class Trainer extends AbstractLoggedComponent {

    private File classifierModelDir;

    private File wikiCacheDir;

    private Map<String, String> id2text;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      classifierModelDir = new File(Objects.firstNonNull(System.getProperty(MODEL_DIR_PROPERTY),
              (String) context.getConfigParameterValue("model-dir")));
      wikiCacheDir = new File(Objects.firstNonNull(System.getProperty(WIKI_CACHE_DIR_PROPERTY),
              (String) context.getConfigParameterValue("wiki-cache-dir")));
      File id2textFile = new File(Objects.firstNonNull(
              System.getProperty(ID_TO_TEXT_FILE_PROPERTY),
              (String) context.getConfigParameterValue("id-to-text-file")));
      // prepare target
      try {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(id2textFile));
        id2text = (Map<String, String>) ois.readObject();
        ois.close();
      } catch (Exception e) {
        throw new ResourceInitializationException(e);
      }
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
        // prepare gs
        List<String> gsIds = Lists.newArrayList();
        for (RetrievalResult gs : RetrievalResultArray.retrieveRetrievalResults(ViewManager
                .getView(jcas, TrecKbaViewType.DOCUMENT_GS_CENTRAL))) {
          gsIds.add(gs.getDocID());
        }
        // do task
        trainRetrieval(input.getQuestion(), keyterms, documents, gsIds);
        // save output
        RetrievalResultArray.storeRetrievalResults(ViewManager.getDocumentView(jcas), documents);
      } catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
      }
    }

    private void trainRetrieval(String question, List<Keyterm> keyterms,
            List<RetrievalResult> documents, List<String> gsIds) throws ClassNotFoundException,
            IOException, ParseException {
      // prepare source
      WikipediaArticleCache.loadCache(new File(wikiCacheDir, question));
      if (question.equals("William_H._Gates,_Sr")) {
        question = "William_H._Gates,_Sr.";
      }
      String title = question.replace('_', ' ');
      String article = WikipediaArticleCache.loadExpandedArticle(title, period, null, null)
              .getValueAt(criticalTime);
      HashMap<String, Double> articleWordCount = SimilarityUtils.countWord(article);
      // add positive instance
      List<Instance> instances = Lists.newArrayList();
      for (RetrievalResult document : documents) {
        String id = document.getDocID();
        if (gsIds.contains(id) && id2text.containsKey(id)) {
          HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
          instances.add(new Instance(Boolean.TRUE, extractFeatures(articleWordCount, textWordCount,
                  document.getProbability())));
        }
      }
      System.out.println("Pos: " + instances.size());
      // add negative instance after sampling
      float ratio = (float) instances.size() / (documents.size() - instances.size());
      for (RetrievalResult document : documents) {
        String id = document.getDocID();
        if (!gsIds.contains(id) && Math.random() < ratio && id2text.containsKey(id)) {
          HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
          instances.add(new Instance(Boolean.FALSE, extractFeatures(articleWordCount,
                  textWordCount, document.getProbability())));
        }
      }
      System.out.println("Pos: " + instances.size());
      // train
      StanfordNLPClassifier classifier = new StanfordNLPClassifier();
      classifier.train(instances);
      classifier.saveModel(new File(classifierModelDir, question));
    }

  }

  public static class Predictor extends AbstractRetrievalUpdater {

    @Override
    protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
            List<RetrievalResult> documents) {
      // TODO Auto-generated method stub
      return null;
    }

  }

  private static List<Feature> extractFeatures(HashMap<String, Double> articleWordCount,
          HashMap<String, Double> textWordCount, float luceneScore) {
    List<Feature> features = Lists.newArrayList();
    features.add(new RealValuedFeature(luceneScore));
    for (Similarity sim : sims) {
      features.add(new RealValuedFeature(sim.getSimilarity(articleWordCount, textWordCount)));
    }
    return features;
  }
}

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
import java.util.Map.Entry;

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
import similarity.Similarity;
import util.SimilarityUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.classifier.AbstractClassifier;
import edu.cmu.cs.ziy.util.classifier.Feature;
import edu.cmu.cs.ziy.util.classifier.Instance;
import edu.cmu.cs.ziy.util.classifier.RealValuedFeature;
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

  private static final String MODEL_ROOT_PROPERTY = "treckba-retrieval.classifier.model-root";

  private static final String WIKI_CACHE_DIR_PROPERTY = "treckba-retrieval.classifier.wiki-cache-dir";

  private static final String ID_TO_TEXT_FILE_PROPERTY = "treckba-retrieval.classifier.id-to-text-file";

  private static final String CLASSPATH_PROPERTY = "treckba-retrieval.classifier.classpath";

  private static final String PROPERTIES_PROPERTY = "treckba-retrieval.classifier.properties";

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
      new KLDivergence(), new JaccardCoefficient(),
      new EuclideanDistance(EuclideanDistance.INVERSE_OF_DISTANCE_PLUS_ONE),
      new EuclideanDistance(EuclideanDistance.EXPONENTIAL_OF_NEGATIVE_DISTANCE),
      new MatchingCoefficient(), new ModifiedOverlapCoefficient(), new AverageKLDivergence() };

  private static final List<String> featureNames = Lists.newArrayList("LuceneScore",
          "CosineSimilarity", "KLDivergence", "JaccardCoefficient", "INVERSE_OF_DISTANCE_PLUS_ONE",
          "EXPONENTIAL_OF_NEGATIVE_DISTANCE", "MatchingCoefficient", "ModifiedOverlapCoefficient",
          "AverageKLDivergence");

  static {
    Instance.setFeatureNames(featureNames);
  }

  public static class Trainer extends AbstractLoggedComponent {

    private File classifierModelDir;

    private File wikiCacheDir;

    private static Map<String, String> id2text;

    private Class<? extends AbstractClassifier> clazz;

    private String propString;

    private Map<String, String> props = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      String classifierModelRootString = Objects.firstNonNull(
              System.getProperty(MODEL_ROOT_PROPERTY),
              (String) context.getConfigParameterValue("model-root"));
      wikiCacheDir = new File(Objects.firstNonNull(System.getProperty(WIKI_CACHE_DIR_PROPERTY),
              (String) context.getConfigParameterValue("wiki-cache-dir")));
      File id2textFile = new File(Objects.firstNonNull(
              System.getProperty(ID_TO_TEXT_FILE_PROPERTY),
              (String) context.getConfigParameterValue("id-to-text-file")));
      try {
        if (id2text == null) {
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(id2textFile));
          id2text = (Map<String, String>) ois.readObject();
          ois.close();
        }
      } catch (Exception e) {
        throw new ResourceInitializationException(e);
      }
      try {
        clazz = (Class<? extends AbstractClassifier>) Class.forName(Objects.firstNonNull(
                System.getProperty(CLASSPATH_PROPERTY),
                (String) context.getConfigParameterValue("classpath")));
      } catch (ClassNotFoundException e) {
        throw new ResourceInitializationException(e);
      }
      propString = Objects.firstNonNull(System.getProperty(PROPERTIES_PROPERTY),
              (String) context.getConfigParameterValue("properties"));
      for (String pair : propString.split(";")) {
        String[] segs = pair.split("=", 2);
        props.put(segs[0], segs[1]);
      }
      classifierModelDir = new File(classifierModelRootString, clazz.getSimpleName() + "-"
              + propString);
      if (!classifierModelDir.exists()) {
        classifierModelDir.mkdir();
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
                .getView(jcas, TrecKbaViewType.DOCUMENT_GS_RELEVANT))) {
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
            IOException, ParseException, InstantiationException, IllegalAccessException {
      // prepare source
      HashMap<String, Double> articleWordCount = getArticleWordCount(question, wikiCacheDir);
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
      System.out.println("Total: " + instances.size());
      // train
      AbstractClassifier classifier = clazz.newInstance();
      for (Entry<String, String> entry : props.entrySet()) {
        classifier.setProperty(entry.getKey(), entry.getValue());
      }
      classifier.setFeatureTypes(instances.get(0).getFeatures());
      classifier.train(instances);
      classifier.saveModel(new File(classifierModelDir, question + ".model"));
      // test
      for (RetrievalResult document : documents) {
        updateDocument(document, id2text, articleWordCount, classifier);
      }
    }
  }

  public static class Predictor extends AbstractRetrievalUpdater {

    private File classifierModelDir;

    private File wikiCacheDir;

    private static Map<String, String> id2text;

    private Class<? extends AbstractClassifier> clazz;

    private String propString;

    private Map<String, String> props = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      String classifierModelRootString = Objects.firstNonNull(
              System.getProperty(MODEL_ROOT_PROPERTY),
              (String) context.getConfigParameterValue("model-root"));
      wikiCacheDir = new File(Objects.firstNonNull(System.getProperty(WIKI_CACHE_DIR_PROPERTY),
              (String) context.getConfigParameterValue("wiki-cache-dir")));
      File id2textFile = new File(Objects.firstNonNull(
              System.getProperty(ID_TO_TEXT_FILE_PROPERTY),
              (String) context.getConfigParameterValue("id-to-text-file")));
      try {
        if (id2text == null) {
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(id2textFile));
          id2text = (Map<String, String>) ois.readObject();
          ois.close();
        }
      } catch (Exception e) {
        throw new ResourceInitializationException(e);
      }
      try {
        clazz = (Class<? extends AbstractClassifier>) Class.forName(Objects.firstNonNull(
                System.getProperty(CLASSPATH_PROPERTY),
                (String) context.getConfigParameterValue("classpath")));
      } catch (ClassNotFoundException e) {
        throw new ResourceInitializationException(e);
      }
      propString = Objects.firstNonNull(System.getProperty(PROPERTIES_PROPERTY),
              (String) context.getConfigParameterValue("properties"));
      for (String pair : propString.split(";")) {
        String[] segs = pair.split("=", 2);
        props.put(segs[0], segs[1]);
      }
      classifierModelDir = new File(classifierModelRootString, clazz.getSimpleName() + "-"
              + propString);
      if (!classifierModelDir.exists()) {
        classifierModelDir.mkdir();
      }
    }

    @Override
    protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
            List<RetrievalResult> documents) {
      // prepare source
      HashMap<String, Double> articleWordCount = null;
      try {
        articleWordCount = getArticleWordCount(question, wikiCacheDir);
      } catch (Exception e) {
        return Lists.newArrayList();
      }
      // test
      AbstractClassifier classifier;
      try {
        classifier = clazz.newInstance();
      } catch (Exception e) {
        return Lists.newArrayList();
      }
      for (Entry<String, String> entry : props.entrySet()) {
        classifier.setProperty(entry.getKey(), entry.getValue());
      }
      try {
        classifier.loadModel(new File(classifierModelDir, question + ".model"));
      } catch (IOException e) {
        return Lists.newArrayList();
      }
      for (RetrievalResult document : documents) {
        updateDocument(document, id2text, articleWordCount, classifier);
      }
      return documents;
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

  private static HashMap<String, Double> getArticleWordCount(String question, File wikiCacheDir)
          throws IOException, ClassNotFoundException {
    WikipediaArticleCache.loadCache(new File(wikiCacheDir, question + ".articles"));
    String title = question.replace('_', ' ');
    if (title.equals("William H. Gates, Sr")) {
      title = "William H. Gates, Sr.";
    }
    String article = WikipediaArticleCache.getExpandedArticle(title, period, null, null)
            .getValueAt(criticalTime);
    HashMap<String, Double> articleWordCount = SimilarityUtils.countWord(article);
    return articleWordCount;
  }

  private static void updateDocument(RetrievalResult document, Map<String, String> id2text,
          HashMap<String, Double> articleWordCount, AbstractClassifier classifier) {
    String id = document.getDocID();
    double probablity;
    if (id2text.containsKey(id)) {
      HashMap<String, Double> textWordCount = SimilarityUtils.countWord(id2text.get(id));
      Instance instance = new Instance(extractFeatures(articleWordCount, textWordCount,
              document.getProbability()));
      probablity = classifier.infer(instance).get(Boolean.TRUE.toString());
      if (Double.isNaN(probablity)) {
        probablity = 0.5;
      }
    } else {
      probablity = 0.5;
    }
    // TODO Change it to GERP style
    document.setComponentId(String.valueOf(document.getProbability()));
    document.setProbablity((float) probablity);
  }
}

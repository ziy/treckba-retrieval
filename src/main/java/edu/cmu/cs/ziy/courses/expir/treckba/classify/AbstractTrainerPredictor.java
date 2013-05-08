package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
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

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.classifier.AbstractClassifier;
import edu.cmu.cs.ziy.util.classifier.Feature;
import edu.cmu.cs.ziy.util.classifier.Instance;
import edu.cmu.cs.ziy.util.classifier.RealValuedFeature;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class AbstractTrainerPredictor extends AbstractLoggedComponent {

  private static final String MODEL_ROOT_PROPERTY = "treckba-retrieval.classifier.model-root";

  private static final String WIKI_CACHE_DIR_PROPERTY = "treckba-retrieval.classifier.wiki-cache-dir";

  private static final String ID_TO_TEXT_FILE_PROPERTY = "treckba-retrieval.classifier.id-to-text-file";

  private static final String CLASSPATH_PROPERTY = "treckba-retrieval.classifier.classpath";

  private static final String PROPERTIES_PROPERTY = "treckba-retrieval.classifier.properties";

  private static final String BACKGROUND_NEGATIVE_PROPERTY = "treckba-retrieval.classifier.background-negative";

  private static final String GS_RELEVANCE_PROPERTY = "treckba-retrieval.classifier.gs-relevance";

  protected static enum GsRelevance {
    CENTRAL, RELEVANT
  }

  protected File classifierModelDir;

  protected File wikiCacheDir;

  protected static Map<String, String> id2text;

  protected Class<? extends AbstractClassifier> clazz;

  protected String propString;

  protected Map<String, String> props = Maps.newHashMap();

  protected boolean backgroundNegative = false;

  protected GsRelevance gsRelevance;

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // read parameters
    String classifierModelRootString = Objects.firstNonNull(
            System.getProperty(MODEL_ROOT_PROPERTY),
            (String) context.getConfigParameterValue("model-root"));
    wikiCacheDir = new File(Objects.firstNonNull(System.getProperty(WIKI_CACHE_DIR_PROPERTY),
            (String) context.getConfigParameterValue("wiki-cache-dir")));
    File id2textFile = new File(Objects.firstNonNull(System.getProperty(ID_TO_TEXT_FILE_PROPERTY),
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
    if (System.getProperty(BACKGROUND_NEGATIVE_PROPERTY) != null) {
      backgroundNegative = Boolean.parseBoolean(System.getProperty(BACKGROUND_NEGATIVE_PROPERTY));
    }
    backgroundNegative = (Boolean) context.getConfigParameterValue("background-negative");
    gsRelevance = GsRelevance.valueOf(Objects.firstNonNull(
            System.getProperty(GS_RELEVANCE_PROPERTY),
            (String) context.getConfigParameterValue("gs-relevance")));
    // locate model dir
    classifierModelDir = new File(classifierModelRootString, clazz.getSimpleName() + "-"
            + propString + "-" + gsRelevance + "-" + backgroundNegative);
    if (!classifierModelDir.exists()) {
      classifierModelDir.mkdir();
    }
  }

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

  protected static List<Feature> extractFeatures(HashMap<String, Double> articleWordCount,
          HashMap<String, Double> textWordCount, float luceneScore) {
    List<Feature> features = Lists.newArrayList();
    features.add(new RealValuedFeature(luceneScore));
    for (Similarity sim : sims) {
      features.add(new RealValuedFeature(sim.getSimilarity(articleWordCount, textWordCount)));
    }
    return features;
  }

  public static HashMap<String, Double> getArticleWordCount(String question, File wikiCacheDir)
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

  public static void updateDocument(RetrievalResult document, Map<String, String> id2text,
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

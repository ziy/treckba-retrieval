package edu.cmu.cs.ziy.courses.expir.treckba.classify;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import edu.cmu.cs.ziy.courses.expir.treckba.view.TrecKbaViewType;
import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.wiki.article.ExpandedWikipediaArticle;
import edu.cmu.cs.ziy.wiki.article.WikipediaArticleCache;
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

  private static final String CACHE_DIR_PROPERTY = "treckba-retrieval.classifier.cache-dir";

  private static final String INDEX_ROOT_PROPERTY = "treckba-retrieval.classifier.index-root";

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

  public static class Trainer extends AbstractLoggedComponent {

    private File classifierDir;

    private File cacheDir;

    private File indexRoot;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      classifierDir = new File(Objects.firstNonNull(System.getProperty(MODEL_DIR_PROPERTY),
              (String) context.getConfigParameterValue("model-dir")));
      cacheDir = new File(Objects.firstNonNull(System.getProperty(CACHE_DIR_PROPERTY),
              (String) context.getConfigParameterValue("cache-dir")));
      indexRoot = new File(Objects.firstNonNull(System.getProperty(INDEX_ROOT_PROPERTY),
              (String) context.getConfigParameterValue("index-root")));
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
        List<RetrievalResult> gs = RetrievalResultArray.retrieveRetrievalResults(ViewManager
                .getView(jcas, TrecKbaViewType.DOCUMENT_GS_CENTRAL));
        // do task
        trainRetrieval(input.getQuestion(), keyterms, documents, gs);
        // save output
        RetrievalResultArray.storeRetrievalResults(ViewManager.getDocumentView(jcas), documents);
      } catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
      }
    }

    private void trainRetrieval(String question, List<Keyterm> keyterms,
            List<RetrievalResult> documents, List<RetrievalResult> gs)
            throws ClassNotFoundException, IOException, ParseException {
      // prepare source
      WikipediaArticleCache.loadCache(new File(cacheDir, question));
      if (question.equals("William_H._Gates,_Sr")) {
        question = "William_H._Gates,_Sr.";
      }
      String title = question.replace('_', ' ');
      String article = WikipediaArticleCache.loadExpandedArticle(title, period, null, null)
              .getValueAt(criticalTime);
      // prepare target
      Map<RetrievalResult, String> doc2dir = Maps.newHashMap();
      SetMultimap<String, RetrievalResult> dir2docs = HashMultimap.create();
      for (RetrievalResult document : documents) {
        Calendar docTime = Calendar.getInstance();
        docTime.setTimeInMillis(Long.parseLong(document.getDocID().substring(0, 10)) * 1000);
        String dir = CalendarUtils.toString(docTime, CalendarUtils.YMDH_FORMAT);
        doc2dir.put(document, dir);
        dir2docs.put(dir, document);
      }
      Map<RetrievalResult, String> doc2text = Maps.newHashMap();
      for (String dir : dir2docs.keySet()) {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexRoot, dir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        for (RetrievalResult doc : dir2docs.get(dir)) {
          TermQuery query = new TermQuery(new Term("stream-id", doc.getDocID()));
          ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
          doc2text.put(doc, searcher.doc(hits[0].doc).getField("body").stringValue());
        }
        reader.close();
      }
      // similarity
      
      // train

    }
  }

}

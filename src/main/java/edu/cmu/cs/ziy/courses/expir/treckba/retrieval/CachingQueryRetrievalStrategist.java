package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.cache.SqliteCacher;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class CachingQueryRetrievalStrategist extends
        AbstractSimpleQueryGenerationRetrievalStrategist {

  private Set<String> queries = Sets.newHashSet();

  private static final String QUERIES_DIR_PROPERTY = "treckba-retrieval.retrieval.queries-dir";

  private static final String CACHE_FILE_PROPERTY = "treckba-retrieval.retrieval.cache-file";

  private File queriesDir;

  private SqliteCacher<ArrayList<IdScorePair>> sqliteCacher;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    queriesDir = new File(Objects.firstNonNull(System.getProperty(QUERIES_DIR_PROPERTY),
            (String) context.getConfigParameterValue("queries-dir")));
    File cacheFile = new File(Objects.firstNonNull(System.getProperty(CACHE_FILE_PROPERTY),
            (String) context.getConfigParameterValue("cache-file")));
    try {
      sqliteCacher = SqliteCacher.open(cacheFile);
    } catch (SqlJetException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected String generateQuery(List<Keyterm> keyterms) {
    String query = super.generateQuery(keyterms);
    queries.add(query);
    return query;
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String query) {
    try {
      List<RetrievalResult> results = Lists.newArrayList();
      for (List<IdScorePair> pairs : sqliteCacher.lookup(query)) {
        for (IdScorePair pair : pairs) {
          results.add(new RetrievalResult(pair.getId(), pair.getScore(), query));
        }
      }
      return results;
    } catch (SqlJetException e) {
      return Lists.newArrayList();
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File queriesFile = new File(queriesDir, String.valueOf(System.currentTimeMillis()));
      Files.write(Joiner.on('\n').join(queries), queriesFile, Charset.defaultCharset());
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}

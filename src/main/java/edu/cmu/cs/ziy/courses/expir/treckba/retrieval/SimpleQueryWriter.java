package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class SimpleQueryWriter extends AbstractSimpleQueryGenerationRetrievalStrategist {

  private static final String QUERIES_DIR_PROPERTY = "treckba-retrieval.retrieval.queries-dir";

  private File queriesDir;

  protected Set<String> allQueries = Sets.newHashSet();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    queriesDir = new File(Objects.firstNonNull(System.getProperty(QUERIES_DIR_PROPERTY),
            (String) context.getConfigParameterValue("queries-dir")));
  }

  @Override
  protected List<String> generateQuery(List<Keyterm> keyterms) {
    List<String> queries = super.generateQuery(keyterms);
    allQueries.addAll(queries);
    return Lists.newArrayList();
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(List<String> queries) {
    return Lists.newArrayList();
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File queriesFile = new File(queriesDir, String.valueOf(System.currentTimeMillis()));
      Files.write(Joiner.on('\n').join(allQueries), queriesFile, Charset.defaultCharset());
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}

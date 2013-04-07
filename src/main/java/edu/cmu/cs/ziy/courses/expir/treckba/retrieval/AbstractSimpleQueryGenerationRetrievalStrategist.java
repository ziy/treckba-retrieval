package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public abstract class AbstractSimpleQueryGenerationRetrievalStrategist extends
        AbstractRetrievalStrategist {

  private static final String QUOTE_PROPERTY = "treckba-retrieval.retrieval.quote";

  protected boolean quote;

  protected QueryParser parser;

  private static final String DIRS_FILE_PROPERTY = "treckba-retrieval.retrieval.dirs-file";

  private static final String INDEX_ROOT_PROPERTY = "treckba-retrieval.retrieval.index-root";

  protected List<File> indexes;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // query
    if (System.getProperty(QUOTE_PROPERTY) != null) {
      quote = Boolean.parseBoolean(System.getProperty(QUOTE_PROPERTY));
    } else {
      quote = (Boolean) context.getConfigParameterValue("quote");
    }
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
    parser = new QueryParser(Version.LUCENE_42, "body", analyzer);
    // retrieval
    String indexDirsFile = Objects.firstNonNull(System.getProperty(DIRS_FILE_PROPERTY),
            (String) context.getConfigParameterValue("dirs-file"));
    List<String> indexDirs = null;
    try {
      indexDirs = Files.readLines(new File(indexDirsFile), Charset.defaultCharset());
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    String indexRoot = Objects.firstNonNull(System.getProperty(INDEX_ROOT_PROPERTY),
            (String) context.getConfigParameterValue("index-root"));
    indexes = Lists.newArrayList();
    for (String indexDir : indexDirs) {
      indexes.add(new File(indexRoot, indexDir));
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    // parse query
    String query = generateQuery(keyterms);
    log("Query: " + query);
    List<RetrievalResult> results = retrieveDocuments(query);
    return results;
  }

  protected abstract List<RetrievalResult> retrieveDocuments(String query);

  protected String generateQuery(List<Keyterm> keyterms) {
    return Joiner.on(' ').join(Lists.transform(keyterms, new KeytermStringGetter(quote)));
  }

  public static class KeytermStringGetter implements Function<Keyterm, String> {

    private boolean quote;

    public KeytermStringGetter(boolean quote) {
      this.quote = quote;
    }

    @Override
    public String apply(Keyterm input) {
      StringBuffer sb = new StringBuffer();
      if (quote) {
        sb.append('\"');
      }
      sb.append(QueryParser.escape(input.getText()));
      if (quote) {
        sb.append('\"');
      }
      return sb.toString();
    }
  }
}

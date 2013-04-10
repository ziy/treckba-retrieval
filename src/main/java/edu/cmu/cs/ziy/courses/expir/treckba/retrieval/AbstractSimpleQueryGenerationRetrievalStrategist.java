package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public abstract class AbstractSimpleQueryGenerationRetrievalStrategist extends
        AbstractRetrievalStrategist {

  private static final String QUOTE_PROPERTY = "treckba-retrieval.retrieval.quote";

  protected boolean quote;

  protected QueryParser parser;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (System.getProperty(QUOTE_PROPERTY) != null) {
      quote = Boolean.parseBoolean(System.getProperty(QUOTE_PROPERTY));
    } else {
      quote = (Boolean) context.getConfigParameterValue("quote");
    }
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
    parser = new QueryParser(Version.LUCENE_42, "body", analyzer);
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    // parse query
    List<String> queries = generateQuery(keyterms);
    log("Query: " + queries);
    List<RetrievalResult> results = retrieveDocuments(queries);
    return results;
  }

  protected abstract List<RetrievalResult> retrieveDocuments(List<String> queries);

  protected List<String> generateQuery(List<Keyterm> keyterms) {
    return Lists.newArrayList(Joiner.on(' ').join(
            Lists.transform(keyterms, new KeytermStringGetter(quote))));
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

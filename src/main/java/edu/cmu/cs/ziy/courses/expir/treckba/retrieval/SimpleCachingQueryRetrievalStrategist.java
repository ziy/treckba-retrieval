package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.cmu.cs.ziy.util.cache.SqliteCacher;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class SimpleCachingQueryRetrievalStrategist extends
        AbstractSimpleQueryGenerationRetrievalStrategist {

  private static final String START_DATE_PROPERTY = "treckba-retrieval.retrieval.start-date";

  private static final String END_DATE_PROPERTY = "treckba-retrieval.retrieval.end-date";

  private int startDateInSeconds;

  private int endDateInSeconds;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");

  private static final String CACHE_FILE_PROPERTY = "treckba-retrieval.retrieval.cache-file";

  private SqliteCacher<ArrayList<IdScorePair>> sqliteCacher;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String startDateString = Objects.firstNonNull(System.getProperty(START_DATE_PROPERTY),
            (String) context.getConfigParameterValue("start-date"));
    String endDateString = Objects.firstNonNull(System.getProperty(END_DATE_PROPERTY),
            (String) context.getConfigParameterValue("end-date"));
    try {
      startDateInSeconds = (int) (sdf.parse(startDateString).getTime() / 1000);
      endDateInSeconds = (int) (sdf.parse(endDateString).getTime() / 1000);
    } catch (ParseException e) {
      throw new ResourceInitializationException(e);
    }

    File cacheFile = new File(Objects.firstNonNull(System.getProperty(CACHE_FILE_PROPERTY),
            (String) context.getConfigParameterValue("cache-file")));
    try {
      sqliteCacher = SqliteCacher.open(cacheFile);
    } catch (SqlJetException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(List<String> queries) {
    try {
      List<RetrievalResult> results = Lists.newArrayList();
      for (String query : queries) {
        for (IdScorePair pair : Iterables.concat(sqliteCacher.lookup(query))) {
          String id = pair.getId();
          float score = pair.getScore();
          int dateInSeconds = Integer.parseInt(id.substring(0, 10));
          if (dateInSeconds >= startDateInSeconds && dateInSeconds < endDateInSeconds
                  && score > 0.1) {
            results.add(new RetrievalResult(id, score, query));
          }
        }
      }
      return results;
    } catch (SqlJetException e) {
      return Lists.newArrayList();
    }
  }

}

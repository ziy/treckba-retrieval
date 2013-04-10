package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class IndividualSearcherRetrievalStrategist extends MultiSearcherRetrievalStrategist {

  protected List<IndexSearcher> searchers;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    IndexReader[] indexReaders = FluentIterable.from(indexes).transform(new IndexReaderOpener())
            .filter(Predicates.notNull()).toArray(IndexReader.class);
    log("Index count: " + indexReaders.length);
    searchers = Lists.newArrayListWithCapacity(indexReaders.length);
    for (IndexReader indexReader : indexReaders) {
      searchers.add(new IndexSearcher(indexReader));
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(List<String> queries) {
    ScoreDoc[] hits;
    List<RetrievalResult> results = Lists.newArrayList();
    try {
      for (IndexSearcher searcher : searchers) {
        hits = searcher.search(parser.parse(queries.get(0)), 100).scoreDocs;
        for (ScoreDoc hit : hits) {
          results.add(new RetrievalResult(searcher.doc(hit.doc).get("stream-id"), hit.score,
                  queries.get(0)));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return results;
  }

}

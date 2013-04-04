package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class IndividualSearcherRetrievalStrategist extends MultiSearcherRetrievalStrategist {

  protected List<IndexSearcher> searchers;

  @Override
  protected void createSearcher(IndexReader[] indexReaders) {
    searchers = Lists.newArrayListWithCapacity(indexReaders.length);
    for (IndexReader indexReader : indexReaders) {
      searchers.add(new IndexSearcher(indexReader));
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String query) {
    ScoreDoc[] hits;
    List<RetrievalResult> results = Lists.newArrayList();
    try {
      for (IndexSearcher searcher : searchers) {
        hits = searcher.search(parser.parse(query), 100).scoreDocs;
        for (ScoreDoc hit : hits) {
          results.add(new RetrievalResult(searcher.doc(hit.doc).get("stream-id"), hit.score, query));
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

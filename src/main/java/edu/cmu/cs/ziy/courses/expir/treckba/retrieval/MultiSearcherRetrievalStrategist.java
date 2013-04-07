package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class MultiSearcherRetrievalStrategist extends AbstractSimpleQueryGenerationRetrievalStrategist {

  private IndexSearcher searcher;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    IndexReader[] indexReaders = FluentIterable.from(indexes).transform(new IndexReaderOpener())
            .filter(Predicates.notNull()).toArray(IndexReader.class);
    log("Index count: " + indexReaders.length);
    MultiReader reader = new MultiReader(indexReaders, true);
    searcher = new IndexSearcher(reader);
  }

  protected List<RetrievalResult> retrieveDocuments(String query) {
    ScoreDoc[] hits;
    List<RetrievalResult> results = Lists.newArrayList();
    try {
      hits = searcher.search(parser.parse(query), 100).scoreDocs;
      for (ScoreDoc hit : hits) {
        results.add(new RetrievalResult(searcher.doc(hit.doc).get("stream-id"), hit.score, query));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return results;
  }

  public static class IndexReaderOpener implements Function<File, IndexReader> {

    @Override
    public IndexReader apply(File dir) {
      try {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(dir));
        System.out.println("[index]" + dir.getName() + " OK");
        return reader;
      } catch (IOException e) {
        System.out.println("[index]" + dir.getName() + " Error");
        return null;
      }
    }
  }

}

package edu.cmu.cs.ziy.courses.expir.treckba.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public class LocalRetrieverCacher extends AbstractRetrieverCacher {

  public LocalRetrieverCacher(File dbFile, File queriesDir, File dirsFile, String indexRoot)
          throws SqlJetException, IOException, ParseException {
    super(dbFile, queriesDir, dirsFile, indexRoot);
  }

  @Override
  protected void retrieveAndCache(Set<String> queries, List<String> dirs, String indexRoot)
          throws IOException, ParseException, SqlJetException {
    for (String dir : dirs) {
      File index = new File(indexRoot, dir);
      if (!index.exists() || !index.isDirectory()) {
        continue;
      }
      SimpleLuceneRetriever retriever = new SimpleLuceneRetriever(Lists.newArrayList(index)
              .toArray(new File[0]), "stream-id", "body");
      Table<String, String, List<IdScorePair>> results = retriever.retrieveDocuments(
              queries.toArray(new String[0]), 100);
      for (Table.Cell<String, String, List<IdScorePair>> result : results.cellSet()) {
        addToCache(result.getRowKey(), result.getColumnKey(),
                (ArrayList<IdScorePair>) result.getValue());
      }
    }
  }

  public static void main(String[] args) throws SqlJetException, IOException, ParseException {
    new LocalRetrieverCacher(new File("retrieval-cache/cache.db3"), new File(
            "retrieval-cache/queries"), new File("../treckba-corpus/corpus/dir-list.txt"),
            "../treckba-corpus/index/");
  }
}

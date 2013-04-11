package edu.cmu.cs.ziy.courses.expir.treckba.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import edu.cmu.cs.ziy.util.cache.SqliteCacher;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public abstract class AbstractRetrieverCacher {

  private SqliteCacher<ArrayList<IdScorePair>> cacher;

  public AbstractRetrieverCacher(File dbFile) throws SqlJetException, IOException, ParseException {
    if (!dbFile.exists()) {
      cacher = SqliteCacher.create(dbFile, SqliteCacher.Type.TEXT);
    } else {
      cacher = SqliteCacher.open(dbFile);
    }
  }

  protected void addToCache(String query, ArrayList<IdScorePair> pairs) throws SqlJetException {
    try {
      cacher.insert(pairs, query);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void addToCache(ListMultimap<String, IdScorePair> query2pairs) throws SqlJetException {
    try {
      cacher.batchWriteStart();
      for (String query : query2pairs.keySet()) {
        cacher.batchInsert(Lists.newArrayList(query2pairs.get(query)), query);
      }
      cacher.batchWriteCommit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void close() throws SqlJetException {
    cacher.close();
  }
}

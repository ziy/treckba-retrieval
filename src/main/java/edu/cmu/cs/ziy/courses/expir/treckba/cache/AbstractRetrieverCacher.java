package edu.cmu.cs.ziy.courses.expir.treckba.cache;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tmatesoft.sqljet.core.SqlJetException;

import edu.cmu.cs.ziy.util.cache.SqliteCacher;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public abstract class AbstractRetrieverCacher {

  private SqliteCacher<ArrayList<IdScorePair>> cacher;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");

  public AbstractRetrieverCacher(File dbFile) throws SqlJetException, IOException, ParseException {
    if (!dbFile.exists()) {
      cacher = SqliteCacher.create(dbFile, SqliteCacher.Type.TEXT, SqliteCacher.Type.INTEGER);
    } else {
      cacher = SqliteCacher.open(dbFile);
    }
  }

  protected void addToCache(String query, String dir, ArrayList<IdScorePair> pairs)
          throws SqlJetException {
    try {
      cacher.insert(pairs, query, (int) sdf.parse(dir).getTime());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void close() throws SqlJetException {
    cacher.close();
  }
}

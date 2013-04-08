package edu.cmu.cs.ziy.courses.expir.treckba.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.cache.SqliteCacher;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public abstract class AbstractRetrieverCacher {

  private SqliteCacher<ArrayList<IdScorePair>> cacher;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");

  public AbstractRetrieverCacher(File dbFile, File queriesDir, File dirsFile, String indexRoot)
          throws SqlJetException, IOException, ParseException {
    if (!dbFile.exists()) {
      cacher = SqliteCacher.create(dbFile, SqliteCacher.Type.TEXT, SqliteCacher.Type.INTEGER);
    } else {
      cacher = SqliteCacher.open(dbFile);
    }
    File[] queriesFile = queriesDir.listFiles();
    Set<String> queries = Sets.newHashSet();
    for (File file : queriesFile) {
      queries.addAll(Files.readLines(file, Charset.defaultCharset()));
    }
    List<String> dirs = Lists.newArrayList();
    dirs = Files.readLines(dirsFile, Charset.defaultCharset());
    retrieveAndCache(queries, dirs, indexRoot);
  }

  protected abstract void retrieveAndCache(Set<String> queries, List<String> dirs, String indexRoot)
          throws IOException, ParseException, SqlJetException;

  protected void addToCache(String query, String dir, ArrayList<IdScorePair> pairs)
          throws SqlJetException {
    try {
      cacher.insert(pairs, query, (int) sdf.parse(dir).getTime());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

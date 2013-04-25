package edu.cmu.cs.ziy.util.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.cache.SetCacheCompressor.FirstFieldGetter;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public class IdsFileGenerator {

  public static void main(String[] args) throws SqlJetException, IOException {
    SqliteCacher.setTransactionCommitFrequency(1000);
    Set<String> queries = Sets.newHashSet();
    // for (File file : new File("retrieval-cache/queries/").listFiles()) {
    // queries.addAll(Collections2.transform(Files.readLines(file, Charsets.UTF_8),
    // new FirstFieldGetter()));
    // }
    queries.addAll(Collections2.transform(
            Files.readLines(new File("retrieval-cache/queries/1365442877843"), Charsets.UTF_8),
            new FirstFieldGetter()));
    SqliteCacher<ArrayList<IdScorePair>> cache = SqliteCacher.open(new File(
            "retrieval-cache/cache.db3.small"));
    Set<String> ids = Sets.newHashSet();
    int i = 0;
    for (String query : queries) {
      System.out.println(i++ + " / " + queries.size());
      for (IdScorePair pair : Iterables.concat(cache.lookup(query))) {
        ids.add(pair.getId());
      }
    }
    Files.write(Joiner.on('\n').join(ids), new File("retrieval-cache/1365442877843-small.ids"),
            Charsets.UTF_8);
  }
}

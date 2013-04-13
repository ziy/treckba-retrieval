package edu.cmu.cs.ziy.util.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.cache.SqliteCacher.Type;

public class SetCacheCompressor {

  public static <V extends Serializable> void compressArrayListValues(File input, File output,
          List<String> keys, Type[] keyTypes) throws SqlJetException, IOException {
    SqliteCacher<ArrayList<V>> oldCache = SqliteCacher.open(input);
    SqliteCacher<ArrayList<V>> newCache = SqliteCacher.create(output, Type.TEXT);
    int i = 0;
    for (String key : keys) {
      System.out.println(i++ + "/" + keys.size());
      Set<V> pairs = Sets.newHashSet(Iterables.concat(oldCache.lookup(key)));
      newCache.batchInsert(Lists.newArrayList(pairs), key);
    }
    oldCache.close();
    newCache.close();
  }

  public static void main(String[] args) throws SqlJetException, IOException {
    SqliteCacher.setTransactionCommitFrequency(1000);
    Set<String> queries = Sets.newHashSet();
    queries.addAll(Collections2.transform(
            Files.readLines(new File("retrieval-cache/queries/1365702825247"), Charsets.UTF_8),
            new FirstFieldGetter()));
    queries.addAll(Collections2.transform(
            Files.readLines(new File("retrieval-cache/queries/1365702825677"), Charsets.UTF_8),
            new FirstFieldGetter()));
    compressArrayListValues(new File("retrieval-cache/cache(copy).db3"), new File(
            "retrieval-cache/cache.db3"), Lists.newArrayList(queries), new Type[] { Type.TEXT });
  }

  public static class FirstFieldGetter implements Function<String, String> {

    @Override
    public String apply(String input) {
      return input.split("\t")[0];
    }

  }
}

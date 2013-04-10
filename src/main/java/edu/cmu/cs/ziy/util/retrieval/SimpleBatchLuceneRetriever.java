package edu.cmu.cs.ziy.util.retrieval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public class SimpleBatchLuceneRetriever {
  
  /**
   * Entry point for batch execution, e.g. condor
   * 
   * @param args
   *          index_root, dir_prefix, queries_dir, id_field, text_field, num_hits, outfile
   * @throws IOException
   * @throws ParseException
   */
  public static void main(String[] args) throws IOException, ParseException {
    String indexRoot = args[0];
    String dirPrefix = args[1];
    String queriesDir = args[2];
    String idField = args[3];
    String textField = args[4];
    int numHits = Integer.parseInt(args[5]);
    String outfile = args[6];

    File[] indexes = new File(indexRoot).listFiles(new PatternFilenameFilter(Pattern
            .quote(dirPrefix) + ".*"));
    Set<String> queries = Sets.newHashSet();
    for (File queriesFile : new File(queriesDir).listFiles()) {
      queries.addAll(Files.readLines(queriesFile, Charsets.UTF_8));
    }
    SimpleLuceneRetriever retriever = new SimpleLuceneRetriever(indexes, idField, textField);
    Table<String, String, List<IdScorePair>> query2dir2pairs = retriever.retrieveDocuments(
            queries.toArray(new String[0]), numHits);
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile));
    oos.writeObject(query2dir2pairs);
    oos.close();
    // System.out.println(dirPrefix + " generate temp file of " + query2dir2pairs.size()
    // + " elements.");
  }
}

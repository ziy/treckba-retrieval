package edu.cmu.cs.ziy.util.retrieval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.guava.RangeSetParser;

public class SimpleLuceneRetriever {

  private File[] indexes;

  private String idField;

  private IndexSearcher[] searchers;

  private QueryParser parser;

  public static final String CLASSPATH = "%HOME/.m2/repository/org/apache/lucene/lucene-core/4.2.0/lucene-core-4.2.0.jar"
          + ":%HOME/.m2/repository/org/apache/lucene/lucene-queryparser/4.2.0/lucene-queryparser-4.2.0.jar"
          + ":%HOME/.m2/repository/org/apache/lucene/lucene-queries/4.2.0/lucene-queries-4.2.0.jar"
          + ":%HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/4.2.0/lucene-analyzers-common-4.2.0.jar"
          + ":%HOME/.m2/repository/com/google/guava/guava/14.0/guava-14.0.jar"
          + ":%PROJECT/target/classes";

  public SimpleLuceneRetriever(File[] indexes, String idField, String textField) throws IOException {
    this.indexes = indexes;
    this.idField = idField;
    searchers = new IndexSearcher[indexes.length];
    for (int i = 0; i < indexes.length; i++) {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(indexes[i]));
      searchers[i] = new IndexSearcher(reader);
      // System.out.println(indexes[i].getName() + " opened.");
    }
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
    parser = new QueryParser(Version.LUCENE_42, textField, analyzer);
  }

  protected List<IdScorePair> retrieveDocuments(IndexSearcher searcher, String query, int numHits)
          throws IOException, ParseException {
    List<IdScorePair> pairs = Lists.newArrayList();
    ScoreDoc[] hits = searcher.search(parser.parse(query), numHits).scoreDocs;
    for (ScoreDoc hit : hits) {
      Document doc = searcher.doc(hit.doc);
      pairs.add(new IdScorePair(doc.get(idField), hit.score));
    }
    return pairs;
  }

  public Map<String, List<IdScorePair>> retrieveDocuments(String query, int numHits)
          throws IOException, ParseException {
    Map<String, List<IdScorePair>> dir2pairs = Maps.newHashMap();
    for (int i = 0; i < indexes.length; i++) {
      dir2pairs.put(indexes[i].getName(), retrieveDocuments(searchers[i], query, numHits));
    }
    return dir2pairs;
  }

  public Table<String, String, List<IdScorePair>> retrieveDocuments(String[] queries, int numHits)
          throws IOException, ParseException {
    Table<String, String, List<IdScorePair>> query2dir2pairs = HashBasedTable.create();
    for (String query : queries) {
      for (int i = 0; i < indexes.length; i++) {
        // System.out.println("retrieved " + query + " from " + indexes[i].getName() + ".");
        query2dir2pairs.put(query, indexes[i].getName(),
                retrieveDocuments(searchers[i], query, numHits));
      }
    }
    return query2dir2pairs;
  }

  /**
   * Entry point directly from command line
   * 
   * @param args
   *          index_root, num_dirs, dir0, dir1, ..., dirn, num_queries, query0, query1, ..., queryn,
   *          id_field, text_field, num_hits, outfile
   * @throws IOException
   * @throws ParseException
   */
  public static void single(String[] args) throws IOException, ParseException {
    String indexRoot = args[0];
    int numDirs = Integer.parseInt(args[1]);
    String[] dirs = Arrays.copyOfRange(args, 2, 2 + numDirs);
    int numQueries = Integer.parseInt(args[2 + numDirs]);
    String[] queries = Arrays.copyOfRange(args, 3 + numDirs, 3 + numDirs + numQueries);
    String idField = args[3 + numDirs + numQueries];
    String textField = args[4 + numDirs + numQueries];
    int numHits = Integer.parseInt(args[5 + numDirs + numQueries]);
    String outfile = args[6 + numDirs + numQueries];

    File[] indexes = new File[numDirs];
    for (int i = 0; i < numDirs; i++) {
      indexes[i] = new File(indexRoot, dirs[i]);
    }

    SimpleLuceneRetriever retriever = new SimpleLuceneRetriever(indexes, idField, textField);
    Table<String, String, List<IdScorePair>> query2dir2pairs = retriever.retrieveDocuments(queries,
            numHits);
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile));
    oos.writeObject(query2dir2pairs);
    oos.close();
  }

  /**
   * Entry point for batch execution, e.g. condor
   * 
   * @param args
   *          index_root, dir_prefix, queries_dir, id_field, text_field, num_hits, outfile
   * @throws IOException
   * @throws ParseException
   */
  public static void batch(String[] args) throws IOException, ParseException {
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
      queries.addAll(Files.readLines(queriesFile, Charset.defaultCharset()));
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

  /**
   * Entry point for batch execution of periodically valid queries
   * 
   * @param args
   *          index_root, dir, queries_dir, id_field, text_field, num_hits, outfile
   * @throws IOException
   * @throws ParseException
   * @throws java.text.ParseException
   */
  public static void main(String[] args) throws IOException, ParseException,
          java.text.ParseException {
    String indexRoot = args[0];
    String dir = args[1];
    String queriesDir = args[2];
    String idField = args[3];
    String textField = args[4];
    int numHits = Integer.parseInt(args[5]);
    String outfile = args[6];

    DateFormat df = new SimpleDateFormat(CalendarUtils.YMDH_FORMAT);
    Calendar date = Calendar.getInstance();
    date.setTime(df.parse(dir));

    Set<String> queries = Sets.newHashSet();
    for (File queriesFile : new File(queriesDir).listFiles()) {
      for (String line : Files.readLines(queriesFile, Charset.defaultCharset())) {
        String[] segs = line.split("\t");
        if (RangeSetParser.parse(segs[1], RangeSetParser.calendarParser(df)).contains(date)) {
          queries.add(segs[0]);
        }
      }
    }
    SimpleLuceneRetriever retriever = new SimpleLuceneRetriever(new File[] { new File(indexRoot,
            dir) }, idField, textField);
    Table<String, String, List<IdScorePair>> query2dir2pairs = retriever.retrieveDocuments(
            queries.toArray(new String[0]), numHits);
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile));
    oos.writeObject(query2dir2pairs);
    oos.close();
    // System.out.println(dirPrefix + " generate temp file of " + query2dir2pairs.size()
    // + " elements.");
  }

  public static class IdScorePair implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private float score;

    public IdScorePair(String id, float score) {
      super();
      this.id = id;
      this.score = score;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      IdScorePair other = (IdScorePair) obj;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      return true;
    }

    public String getId() {
      return id;
    }

    public float getScore() {
      return score;
    }

  }
}

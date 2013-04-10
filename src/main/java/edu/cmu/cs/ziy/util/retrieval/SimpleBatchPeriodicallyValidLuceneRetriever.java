package edu.cmu.cs.ziy.util.retrieval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.guava.RangeSetParser;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public class SimpleBatchPeriodicallyValidLuceneRetriever {
  
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
      for (String line : Files.readLines(queriesFile, Charsets.UTF_8)) {
        String[] segs = line.split("\t");
        if (RangeSetParser.parse(segs[1], RangeSetParser.calendarParser(df)).contains(date)) {
          queries.add(segs[0]);
        }
      }
    }
    System.out.println(dir + " allows " + queries.size() + " queries.");
    SimpleLuceneRetriever retriever = new SimpleLuceneRetriever(new File[] { new File(indexRoot,
            dir) }, idField, textField);
    Table<String, String, List<IdScorePair>> query2dir2pairs = retriever.retrieveDocuments(
            queries.toArray(new String[0]), numHits);
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile));
    oos.writeObject(query2dir2pairs);
    oos.close();
    System.out.println(dir + " generate temp file of " + query2dir2pairs.size() + " elements.");
  }
}

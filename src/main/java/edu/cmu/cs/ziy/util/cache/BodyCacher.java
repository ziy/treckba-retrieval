package edu.cmu.cs.ziy.util.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Files;

import edu.cmu.cs.ziy.util.CalendarUtils;

public class BodyCacher {

  public BodyCacher(File idsFile, File indexRoot, File cacheFile) throws IOException,
          ParseException {
    List<String> ids = Files.readLines(idsFile, Charsets.UTF_8);
    SetMultimap<String, String> dir2ids = HashMultimap.create();
    for (String id : ids) {
      Calendar docTime = CalendarUtils.getGmtInstance(Long.parseLong(id.substring(0, 10)) * 1000);
      String dir = CalendarUtils.toGmtString(docTime, CalendarUtils.YMDH_FORMAT);
      dir2ids.put(dir, id);
    }
    List<String> dirs = Lists.newArrayList(indexRoot.list(DirectoryFileFilter.INSTANCE));
    Collections.sort(dirs);
    Map<String, String> id2text = Maps.newHashMap();
    for (String dir : dirs) {
      System.out.println(dir);
      if (dir2ids.get(dir) == null || dir2ids.get(dir).size() == 0) {
        continue;
      }
      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexRoot, dir)));
      IndexSearcher searcher = new IndexSearcher(reader);
      BooleanQuery query = new BooleanQuery();
      query.setMinimumNumberShouldMatch(1);
      for (String id : dir2ids.get(dir)) {
        query.add(new TermQuery(new Term("stream-id", id)), Occur.SHOULD);
      }
      ScoreDoc[] hits = searcher.search(query, dir2ids.get(dir).size()).scoreDocs;
      for (ScoreDoc hit : hits) {
        Document doc = searcher.doc(hit.doc);
        id2text.put(doc.getField("stream-id").stringValue(), doc.getField("body").stringValue());
      }
      reader.close();
    }
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
    System.out.println(id2text.size());
    oos.writeObject(id2text);
    oos.close();
  }

  public static void main(String[] args) throws IOException, ParseException {
    // new BodyCacher(new File("retrieval-cache/ids.small.quote"), new File(
    // "/home/yangzi/sshfs/treckba-corpus/index"), new File("retrieval-cache/id2text"));
    new BodyCacher(new File(args[0]), new File(args[1]), new File(args[2]));
  }
}

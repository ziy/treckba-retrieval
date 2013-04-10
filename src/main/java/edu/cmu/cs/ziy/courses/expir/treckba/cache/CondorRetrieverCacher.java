package edu.cmu.cs.ziy.courses.expir.treckba.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;

import condorAPI.Cluster;
import condorAPI.Condor;
import condorAPI.CondorException;
import condorAPI.JobDescription;
import edu.cmu.cs.ziy.util.retrieval.SimpleBatchPeriodicallyValidLuceneRetriever;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever;
import edu.cmu.cs.ziy.util.retrieval.SimpleLuceneRetriever.IdScorePair;

public class CondorRetrieverCacher extends AbstractRetrieverCacher {

  public CondorRetrieverCacher(File dbFile, File queriesDir, File dirsFile, File indexRoot,
          String homeRoot, String projectDir) throws SqlJetException, IOException, ParseException,
          CondorException, InterruptedException, ClassNotFoundException {
    super(dbFile);
    // Set<String> dirPrefixes = Sets.newHashSet(FluentIterable
    // .from(Files.readLines(dirsFile, Charsets.UTF_8))
    // .filter(new ExistAndIsDir(indexRoot)).transform(new FixedLengthPrefixGetter(10)));
    Set<String> dirPrefixes = Sets.newHashSet(FluentIterable.from(
            Files.readLines(dirsFile, Charsets.UTF_8)).filter(new ExistAndIsDir(indexRoot)));
    File tempDir = new File(projectDir, "tmp");
    if (tempDir.exists()) {
      FileUtils.deleteDirectory(tempDir);
    }
    tempDir.mkdir();
    Condor condor = new Condor(new File(tempDir, "main.log").getCanonicalPath());
    Set<File> tempFiles = Sets.newHashSet();
    for (String dirPrefix : dirPrefixes) {
      File tempFile = File.createTempFile(dirPrefix + "-", null, tempDir);
      tempFiles.add(tempFile);
      String arguments = String.format(
              "-cp %s %s %s %s %s %s %s %s %s",
              SimpleLuceneRetriever.CLASSPATH.replace("%HOME", homeRoot).replace("%PROJECT",
                      projectDir),
              SimpleBatchPeriodicallyValidLuceneRetriever.class.getCanonicalName(),
              indexRoot.getCanonicalFile(), dirPrefix, queriesDir, "stream-id", "body", 100,
              tempFile.getCanonicalPath());
      JobDescription jd = new JobDescription();
      jd.addAttribute("universe", "vanilla");
      jd.addAttribute("executable", "/usr/java/latest/bin/java");
      jd.addAttribute("arguments", arguments);
      // jd.addAttribute("log", new File(tempDir, dirPrefix + ".condor").getCanonicalPath());
      jd.addAttribute("output", new File(tempDir, dirPrefix + ".log").getCanonicalPath());
      jd.addAttribute("error", new File(tempDir, dirPrefix + ".err").getCanonicalPath());
      jd.addQueue();
      Cluster c = condor.submit(jd);
      System.out.println("submitted " + c + " for " + dirPrefix + ".");
    }
    while (true) {
      int numJobs = condor.getAliveJobs().size();
      System.out.println(numJobs + " jobs remaining.");
      if (numJobs <= 0) {
        break;
      }
      Thread.sleep(20 * 1000);
    }

    int i = 0;
    for (File tempFile : tempFiles) {
      if (tempFile.length() <= 0) {
        continue;
      }
      System.out.println("adding " + i++ + ": " + tempFile.getName() + ".");
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile));
      @SuppressWarnings("unchecked")
      Table<String, String, List<IdScorePair>> results = (Table<String, String, List<IdScorePair>>) ois
              .readObject();
      ois.close();
      for (Table.Cell<String, String, List<IdScorePair>> result : results.cellSet()) {
        addToCache(result.getRowKey(), result.getColumnKey(),
                (ArrayList<IdScorePair>) result.getValue());
      }
    }
  }

  public static void main(String[] args) throws SqlJetException, IOException, ParseException,
          CondorException, InterruptedException, ClassNotFoundException {
    CondorRetrieverCacher crc = new CondorRetrieverCacher(new File(args[0]), new File(args[1]),
            new File(args[2]), new File(args[3]), args[4], args[5]);
    // CondorRetrieverCacher crc = new CondorRetrieverCacher(new File("retrieval-cache/cache.db3"),
    // new File("retrieval-cache/queries"), new File("../treckba-corpus/corpus/dir-list.txt"),
    // new File("../treckba-corpus/index/"), "/bos/usr7/ziy", ".");
    crc.close();
  }

  public static class FixedLengthPrefixGetter implements Function<String, String> {

    private int length;

    public FixedLengthPrefixGetter(int length) {
      this.length = length;
    }

    @Override
    public String apply(String input) {
      return input.substring(0, length);
    }

  }

  public static class ExistAndIsDir implements Predicate<String> {

    private File root;

    public ExistAndIsDir(File indexRoot) {
      this.root = indexRoot;
    }

    @Override
    public boolean apply(String input) {
      File file = new File(root, input);
      return file.exists() && file.isDirectory();
    }

  }
}

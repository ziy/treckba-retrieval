package edu.cmu.cs.ziy.courses.expir.treckba.retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public abstract class AbstractSimpleQueryGenerationLuceneRetrievalStrategist extends
        AbstractSimpleQueryGenerationRetrievalStrategist {

  private static final String DIRS_FILE_PROPERTY = "treckba-retrieval.retrieval.dirs-file";

  private static final String INDEX_ROOT_PROPERTY = "treckba-retrieval.retrieval.index-root";

  protected List<File> indexes;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String indexDirsFile = Objects.firstNonNull(System.getProperty(DIRS_FILE_PROPERTY),
            (String) context.getConfigParameterValue("dirs-file"));
    List<String> indexDirs = null;
    try {
      indexDirs = Files.readLines(new File(indexDirsFile), Charset.defaultCharset());
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    String indexRoot = Objects.firstNonNull(System.getProperty(INDEX_ROOT_PROPERTY),
            (String) context.getConfigParameterValue("index-root"));
    indexes = Lists.newArrayList();
    for (String indexDir : indexDirs) {
      indexes.add(new File(indexRoot, indexDir));
    }
  }

}

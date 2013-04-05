package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.io.Resources;

import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.collection.IterableCollectionReader;

public final class TrecKbaTopicCollectionReader extends IterableCollectionReader {

  private static final String INPUT_PROPERTY = "treckba-retrieval.collection.input";

  @Override
  protected Iterator<DataElement> getInputSet() throws ResourceInitializationException {
    String input = Objects.firstNonNull(System.getProperty(INPUT_PROPERTY),
            (String) getConfigParameterValue("input"));
    try {
      return new TrecKbaTopicElementIterator(input);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  public final class TrecKbaTopicElementIterator implements Iterator<DataElement> {

    private int ptr;

    private String[] topics;

    public TrecKbaTopicElementIterator(String json) throws IOException {
      this.ptr = 0;
      Reader jsonReader = new InputStreamReader(Resources.getResource(getClass(), json)
              .openStream());
      this.topics = TrecKbaTopics.readTrecKbaTopics(jsonReader).getTopicNames();
    }

    @Override
    public boolean hasNext() {
      return ptr < topics.length;
    }

    @Override
    public DataElement next() {
      DataElement input = new DataElement(getDataset(), Integer.toString(ptr), topics[ptr], null);
      ptr++;
      return input;
    }

    @Override
    public void remove() {
    }
    
    public String getTopicAt(int index) {
      return topics[index];
    }

  }
}

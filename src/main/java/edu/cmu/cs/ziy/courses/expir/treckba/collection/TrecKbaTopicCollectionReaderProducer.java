package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.IOException;
import java.util.Map;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.collection.AbstractCollectionReaderProducer;

public class TrecKbaTopicCollectionReaderProducer extends AbstractCollectionReaderProducer {

  private TrecKbaTopicCollectionReader reader;

  @Override
  public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
          throws ResourceInitializationException {
    reader = new TrecKbaTopicCollectionReader();
    reader.initialize(aSpecifier, aAdditionalParams);
    return super.initialize(aSpecifier, aAdditionalParams);
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return reader.hasNext();
  }

  @Override
  protected String getDataset() {
    return reader.getDataset();
  }

  @Override
  protected int getStageId() {
    return reader.getStageId();
  }

  @Override
  protected DataElement getNextFromSource() throws Exception {
    return reader.getNextElement();
  }

  @Override
  protected String getUUID() {
    return reader.getUUID();
  }

}

package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.util.Map;

import javax.jms.MapMessage;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import edu.cmu.cs.ziy.courses.expir.treckba.collection.TrecKbaTopicCollectionReader.TrecKbaTopicElementIterator;
import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.collection.AbstractCollectionReaderConsumer;

public class TrecKbaTopicCollectionReaderConsumer extends AbstractCollectionReaderConsumer {

  private TrecKbaTopicCollectionReader reader;

  private TrecKbaTopicElementIterator iterator;

  @Override
  public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
          throws ResourceInitializationException {
    reader = new TrecKbaTopicCollectionReader();
    reader.initialize(aSpecifier, aAdditionalParams);
    iterator = ((TrecKbaTopicElementIterator) reader.getInputSet());
    return super.initialize(aSpecifier, aAdditionalParams);
  }

  @Override
  protected DataElement getDataElement(MapMessage map) throws Exception {
    String dataset = map.getString("dataset");
    String id = map.getString("sequenceId");
    return new DataElement(dataset, id, iterator.getTopicAt(Integer.parseInt(id)), null);
  }

}

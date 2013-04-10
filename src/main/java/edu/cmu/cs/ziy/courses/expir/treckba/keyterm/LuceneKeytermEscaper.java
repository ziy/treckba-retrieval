package edu.cmu.cs.ziy.courses.expir.treckba.keyterm;

import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser;

import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class LuceneKeytermEscaper extends AbstractKeytermUpdater {

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    List<Keyterm> newKeyterms = Lists.newArrayList();
    for (Keyterm keyterm : keyterms) {
      String text = keyterm.getText();
      if (text.length() > 80) {
        continue;
      } else {
        text = text.replaceAll("[\\s\\n\\r]+", " ");
        text = QueryParser.escape(text);
      }
      String componentId = keyterm.getComponentId();
      float probablity = keyterm.getProbability();
      Keyterm newKeyterm = new Keyterm(text);
      newKeyterm.setComponentId(componentId);
      newKeyterm.setProbablity(probablity);
      newKeyterms.add(newKeyterm);
    }
    return newKeyterms;
  }
}

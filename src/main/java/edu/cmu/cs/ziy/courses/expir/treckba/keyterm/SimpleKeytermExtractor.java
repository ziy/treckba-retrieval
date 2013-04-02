package edu.cmu.cs.ziy.courses.expir.treckba.keyterm;

import java.util.List;

import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class SimpleKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    if (question.equals("William_H._Gates,_Sr")) {
      question = "William_H._Gates,_Sr.";
    }
    String keyterm = extractKeyterm(question);
    List<Keyterm> keyterms = Lists.newArrayList(new Keyterm(keyterm));
    log(keyterms.toString());
    return keyterms;
  }

  protected String extractKeyterm(String keyterm) {
    String newKeyterm = keyterm.replace('_', ' ');
    newKeyterm = newKeyterm.replaceAll("Category:", "");
    newKeyterm = newKeyterm.replaceAll("List of ", "");
    newKeyterm = newKeyterm.replaceAll("\\s*\\(.*?\\)\\s*", "");
    return newKeyterm;
  }
}

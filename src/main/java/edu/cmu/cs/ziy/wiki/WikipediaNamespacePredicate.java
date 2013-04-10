package edu.cmu.cs.ziy.wiki;

import java.io.IOException;

import org.wikipedia.Wiki;

import com.google.common.base.Predicate;

public class WikipediaNamespacePredicate implements Predicate<String> {

  private Wiki wiki;

  private int namespace;

  public WikipediaNamespacePredicate(Wiki wiki, int namespace) {
    this.wiki = wiki;
    this.namespace = namespace;
  }

  @Override
  public boolean apply(String input) {
    try {
      return wiki.namespace(input) == namespace;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

}

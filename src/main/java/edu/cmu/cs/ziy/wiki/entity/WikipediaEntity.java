package edu.cmu.cs.ziy.wiki.entity;

import java.io.Serializable;
import java.util.Calendar;

import com.google.common.base.Predicate;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import edu.cmu.cs.ziy.util.CalendarUtils;
import edu.cmu.cs.ziy.util.DefaultPeriodicallyValidObject;

public class WikipediaEntity extends DefaultPeriodicallyValidObject implements Serializable {

  private static final long serialVersionUID = 1L;

  // DONE Create subclasses for each relation, and force validity check while the entity is created.
  // DONE Category them into two super classes: REDIRECT, INLINK -> External, CATEGORY, OUTLINK,
  // BOLD TEXT -> Internal
  public static enum Relation {
    ORIGINAL, REDIRECT, CATEGORY, INLINK, OUTLINK, INLINK_ANCHOR, OUTLINK_ANCHOR, BOLDTEXT
  };

  protected String text;

  protected Relation relation;

  protected WikipediaEntity(String text, Relation relation, RangeSet<Calendar> periods) {
    super(periods);
    this.text = text;
    this.relation = relation;
  }

  public static WikipediaEntity newInvalidInstance(String text, Relation relation) {
    return new WikipediaEntity(text, relation, TreeRangeSet.<Calendar> create());
  }

  public static WikipediaEntity newEternalInstance(String text, Relation relation) {
    RangeSet<Calendar> periods = TreeRangeSet.create();
    periods.add(Range.closed(CalendarUtils.BIG_BANG, CalendarUtils.BIG_RIP));
    return new WikipediaEntity(text, relation, periods);
  }

  public static WikipediaEntity newPresentInstance(String text, Relation relation,
          Calendar creationTime) {
    RangeSet<Calendar> periods = TreeRangeSet.create();
    periods.add(Range.closed(creationTime, CalendarUtils.PRESENT));
    return new WikipediaEntity(text, relation, periods);
  }

  public static WikipediaEntity newDeletedInstance(String text, Relation relation,
          Calendar deletionTime) {
    RangeSet<Calendar> periods = TreeRangeSet.create();
    periods.add(Range.closedOpen(CalendarUtils.BIG_BANG, deletionTime));
    return new WikipediaEntity(text, relation, periods);
  }

  public static WikipediaEntity newInstance(String text, Relation relation,
          RangeSet<Calendar> periods) {
    return new WikipediaEntity(text, relation, periods);
  }

  public static class RelationPredicate implements Predicate<WikipediaEntity> {

    private Relation relation;

    public RelationPredicate(Relation relation) {
      this.relation = relation;
    }

    @Override
    public boolean apply(WikipediaEntity input) {
      return input.relation.equals(relation);
    }

  }

  @Override
  public String toString() {
    return "[" + relation + "]" + text + ":" + super.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((relation == null) ? 0 : relation.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    WikipediaEntity other = (WikipediaEntity) obj;
    if (relation != other.relation)
      return false;
    if (text == null) {
      if (other.text != null)
        return false;
    } else if (!text.equals(other.text))
      return false;
    return true;
  }

  public String getText() {
    return text;
  }

  public Relation getRelation() {
    return relation;
  }

}

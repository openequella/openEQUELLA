/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.taxonomy.impl;

import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.terms.Term;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.taxonomy.TermDao;
import com.tle.core.taxonomy.TermResult;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(TermDao.class)
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class TermDaoImpl extends GenericDaoImpl<Term, Long> implements TermDao {
  /**
   * We can't tell Hibernate to create new TermResult objects because it's not in a plug-in. We need
   * to set TERM_RESULT_TRANSFORMER as the ResultTransformer for any queries.
   */
  private static final String TERM_RESULT_PROJECTION =
      "SELECT t.value, t.fullValue, t.left, t.right, t.uuid ";

  private static final ResultTransformer TERM_RESULT_TRANSFORMER =
      new BasicTransformerAdapter() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
          return new TermResult(
              (String) tuple[0],
              (String) tuple[1],
              ((Integer) tuple[2]) == ((Integer) tuple[3]) - 1,
              (String) tuple[4]);
        }
      };

  private static final String ROOT_TERMS_QUERY =
      "FROM Term t WHERE t.taxonomy = ? AND t.parent IS NULL ORDER BY t.left ASC";
  private static final String ROOT_TERM_RESULTS_QUERY = TERM_RESULT_PROJECTION + ROOT_TERMS_QUERY;

  private static final String CHILD_TERMS_QUERY =
      "FROM Term t WHERE t.parent = ? ORDER BY t.left ASC";
  private static final String CHILD_TERM_RESULTS_QUERY = TERM_RESULT_PROJECTION + CHILD_TERMS_QUERY;

  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(TermDaoImpl.class);

  public TermDaoImpl() {
    super(Term.class);
  }

  @Override
  public String getDataForTerm(Term term, String key) {
    List<String> results =
        getHibernateTemplate()
            .find(
                "SELECT ta.value FROM TermAttributes ta WHERE ta.term = ? AND ta.key = ?",
                new Object[] {term, key});
    return Check.isEmpty(results) ? null : results.get(0);
  }

  @Override
  public List<Term> getAllTermsInOrder(Taxonomy taxonomy) {
    return getHibernateTemplate()
        .find("FROM Term t WHERE t.taxonomy = ? ORDER BY t.left", new Object[] {taxonomy});
  }

  @Override
  public Term getTerm(final Taxonomy taxonomy, final String termFullPath) {
    return (Term)
        getHibernateTemplate()
            .execute(
                session -> {
                  Query q =
                      session.createQuery(
                          "FROM Term WHERE taxonomy = :taxonomy" + " AND fullValue = :fullValue");
                  q.setParameter("taxonomy", taxonomy);
                  q.setString("fullValue", termFullPath);
                  return q.uniqueResult();
                });
  }

  @Override
  public Term getTermByUuid(final Taxonomy taxonomy, final String termUuid) {
    return (Term)
        getHibernateTemplate()
            .execute(
                session -> {
                  Query q =
                      session.createQuery(
                          "FROM Term WHERE uuid = :uuid" + " AND taxonomy = :taxonomy");
                  q.setParameter("uuid", termUuid);
                  q.setParameter("taxonomy", taxonomy);
                  return q.uniqueResult();
                });
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void delete(final Term term) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) {
                final Taxonomy taxonomy = term.getTaxonomy();
                final int left = term.getLeft();
                final int right = term.getRight();

                Query q =
                    session.createQuery(
                        "DELETE FROM TermAttributes ta where ta.term.id in (FROM Term"
                            + " WHERE lft BETWEEN :left AND :right AND taxonomy = :taxonomy)");
                q.setInteger("left", left);
                q.setInteger("right", right);
                q.setParameter("taxonomy", taxonomy);
                q.executeUpdate();

                q =
                    session.createQuery(
                        "DELETE FROM Term"
                            + " WHERE lft BETWEEN :left AND :right AND taxonomy = :taxonomy");
                q.setInteger("left", left);
                q.setInteger("right", right);
                q.setParameter("taxonomy", taxonomy);
                q.executeUpdate();

                shiftLeftAndRightIndexes(
                    session, taxonomy, right + 1, Integer.MAX_VALUE, -(right - left + 1));

                return null;
              }
            });
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public Term insertNewTerm(
      Taxonomy taxonomy, Term parent, @Nullable String termUuid, String termValue, int index) {
    termValue = termValue.trim();
    checkTermValue(termValue, taxonomy, parent);

    final int left =
        parent != null
            ? getLeftForChildIndex(parent, index)
            : getLeftForRootTermIndex(taxonomy, index);

    shiftLeftAndRightIndexes(null, taxonomy, left, Integer.MAX_VALUE, 2);

    final String fullValue =
        parent == null
            ? termValue
            : parent.getFullValue() + TaxonomyConstants.TERM_SEPARATOR + termValue;

    final Term newChild = new Term();
    newChild.setUuid(termUuid == null ? UUID.randomUUID().toString() : termUuid);
    newChild.setValue(termValue);
    newChild.setLeft(left);
    newChild.setRight(left + 1);
    newChild.setParent(parent);
    newChild.setTaxonomy(taxonomy);
    newChild.setFullValue(fullValue);

    save(newChild);

    return newChild;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  protected void postSave(Term entity) {
    super.postSave(entity);
    // dangerous to keep terms in cache because of the shiftLeftAndRightIndexes magic
    // (meaning isLeaf and paths becomes out of date)
    flush();
    clear();
  }

  private int getLeftForChildIndex(Term parent, int index) {
    if (index == 0) {
      return parent.getLeft() + 1;
    }

    if (index < 0) {
      return parent.getRight();
    }

    final List<Term> children = getChildTerms(parent);
    if (children.isEmpty()) {
      return parent.getLeft() + 1;
    }

    if (index >= children.size()) {
      return parent.getRight();
    }

    return children.get(index).getLeft();
  }

  private int getLeftForRootTermIndex(Taxonomy taxonomy, int index) {
    if (index == 0) {
      return 0;
    }

    final List<Term> roots = getRootTerms(taxonomy);
    if (roots.isEmpty()) {
      return 0;
    }

    if (index >= roots.size() || index < 0) {
      return roots.get(roots.size() - 1).getRight() + 1;
    }

    return roots.get(index).getLeft();
  }

  @Override
  public List<Term> getRootTerms(Taxonomy taxonomy) {
    return getHibernateTemplate().find(ROOT_TERMS_QUERY, taxonomy);
  }

  @Override
  public List<TermResult> getRootTermResults(final Taxonomy taxonomy) {
    return getHibernateTemplate()
        .executeFind(
            session -> {
              Query q = session.createQuery(ROOT_TERM_RESULTS_QUERY);
              q.setParameter(0, taxonomy);
              q.setResultTransformer(TERM_RESULT_TRANSFORMER);
              return q.list();
            });
  }

  @Override
  public List<String> getRootTermValues(final Taxonomy taxonomy) {
    return getHibernateTemplate()
        .executeFind(
            session -> {
              Query q = session.createQuery("SELECT value " + ROOT_TERMS_QUERY);
              q.setParameter(0, taxonomy);
              return q.list();
            });
  }

  @Override
  public List<Term> getChildTerms(Term parentTerm) {
    return getHibernateTemplate().find(CHILD_TERMS_QUERY, parentTerm);
  }

  @Override
  public List<TermResult> getChildTermResults(final Term parentTerm) {
    return getHibernateTemplate()
        .executeFind(
            session -> {
              Query q = session.createQuery(CHILD_TERM_RESULTS_QUERY);
              q.setParameter(0, parentTerm);
              q.setResultTransformer(TERM_RESULT_TRANSFORMER);
              return q.list();
            });
  }

  @Override
  public List<String> getChildTermValues(Term parentTerm) {
    return getHibernateTemplate().find("SELECT value " + CHILD_TERMS_QUERY, parentTerm);
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void move(final Term moveThisTerm, final Term parent, int index) {
    // Sanity Check
    if (moveThisTerm == null) {
      throw new RuntimeException("Could not find the term requested to move");
    }
    // Sanity Check
    if (parent != null && moveThisTerm.equals(parent)) {
      throw new RuntimeException("Attempted to re-parent term under itself");
    }

    // AL, AR, AW = left, right and width of term that we are try to move.
    // INC/DEC FROM = left/right value that should be INC/DECed from the
    // source AL/AR.

    final Taxonomy taxonomy = moveThisTerm.getTaxonomy();

    final int al = moveThisTerm.getLeft();
    final int ar = moveThisTerm.getRight();
    final int aw = ar - al + 1;

    final int incFrom;
    final int decFrom;
    if (parent == null) {
      final List<Term> newSibs = getRootTerms(taxonomy);
      if (index == 0) {
        // Move to first root node
        incFrom = 0;
        decFrom = Integer.MIN_VALUE; // Invalid case - negative value
        // required for later check to
        // indicate this is a
        // "move left".
      } else if (index < 0 || index >= newSibs.size()) {
        // Move to last root node
        incFrom = Integer.MAX_VALUE; // Invalid case - positive value
        // required for later checks to
        // indicate this is a
        // "move right".
        decFrom = newSibs.get(newSibs.size() - 1).getRight();
      } else {
        // Move to a root node at index
        final Term destTerm = newSibs.get(index);
        incFrom = destTerm.getLeft();
        // Special edge case when moving term to be a later sibling when
        // it is already a root term, since the left of the dest moves
        // due to the removal of our term.
        decFrom = moveThisTerm.getParent() == null ? destTerm.getRight() : destTerm.getLeft() - 1;
      }
    } else {
      final List<Term> newSibs = getChildTerms(parent);
      if (newSibs.isEmpty()) {
        // Move to an empty parent
        incFrom = parent.getRight();
        decFrom = parent.getLeft();
      } else if (index == 0) {
        // Move to first child
        incFrom = parent.getLeft() + 1;
        decFrom = parent.getLeft();
      } else if (index < 0 || index >= newSibs.size()) {
        // Move to last child
        incFrom = parent.getRight();
        decFrom = parent.getRight() - 1;
      } else {
        // Move to child at index
        final Term destTerm = newSibs.get(index);
        incFrom = destTerm.getLeft();

        // Special edge case when moving term to be a later sibling in
        // the same parent, since the left of the dest term moves due to
        // the removal of our term.
        decFrom =
            Objects.equals(moveThisTerm.getParent(), parent)
                ? destTerm.getRight()
                : destTerm.getLeft() - 1;
      }
    }

    // Sanity Check
    if (al == incFrom) {
      throw new RuntimeException("Term is already at that exact destination");
    }

    // Sanity Check
    if ((al < incFrom && incFrom < ar) || (al < decFrom && decFrom < ar)) {
      throw new RuntimeException(
          "Attempted to re-parent term under itself or one of its children.");
    }

    getHibernateTemplate()
        .execute(
            session -> {
              // Find the largest Right value, plus one. This will be our
              // temporary Left position for the block of terms we are moving.
              final int maxLeft =
                  (Integer)
                      session
                          .createQuery("SELECT MAX(rht) + 1 FROM Term WHERE taxonomy = :taxonomy")
                          .setParameter("taxonomy", taxonomy)
                          .uniqueResult();

              // Temporarily move the term and children to the end
              shiftLeftAndRightIndexes(session, taxonomy, al, ar, maxLeft - al);
              // shift(session, taxonomy, al, ar, maxLeft - al);

              // destLeft should eventually contain the exact left index that
              // the term being moved will use.
              int destLeft = -1;

              // Both AL and AR will be greater than INCFROM and DECFROM, or
              // they will both be lower. It doesn't matter which of these
              // pairs of variable we test for.
              if (ar < incFrom) {
                // Term was before destination, so shift stuff down
                shiftLeftAndRightIndexes(session, taxonomy, ar + 1, decFrom, -aw);
                // shift(session, taxonomy, ar + 1, decFrom, -aw);
                destLeft = decFrom - aw + 1;
              } else {
                // Term was after destination, so shift stuff up
                shiftLeftAndRightIndexes(session, taxonomy, incFrom, al - 1, aw);
                // shift(session, taxonomy, incFrom, al - 1, aw);
                destLeft = incFrom;
              }

              // Move term back from the temporary position
              shiftLeftAndRightIndexes(
                  session, taxonomy, maxLeft, maxLeft + aw - 1, -(maxLeft - destLeft));
              // shift(session, taxonomy, maxLeft, maxLeft + aw - 1, -(maxLeft - destLeft));

              // We need to update the lineage if parent has changed. The left
              // and right need to be temporarily set on the object so that we
              // only invalidate full values of the current and child terms.
              Term movedTermParent = moveThisTerm.getParent();
              if (!Objects.equals(movedTermParent, parent)
                  || (parent != null
                      && movedTermParent != null
                      && !Objects.equals(movedTermParent.getFullValue(), parent.getFullValue()))) {
                moveThisTerm.setParent(parent);
                moveThisTerm.setLeft(destLeft);
                moveThisTerm.setRight(destLeft + aw - 1);

                invalidateFullValues(moveThisTerm);
                updateFullValues(moveThisTerm.getTaxonomy());
              }

              return null;
            });
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void renameTermValue(Term term, String newValue) {
    final Taxonomy taxonomy = term.getTaxonomy();

    newValue = newValue.trim();
    checkTermValue(newValue, taxonomy, term.getParent());

    term.setValue(newValue);
    try {
      save(term);
      invalidateFullValues(term);
      updateFullValues(taxonomy);
    } catch (DataIntegrityViolationException e2) {
      throw new DataIntegrityViolationException("SIBLING_CHECK");
    }
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteForTaxonomy(final Taxonomy taxonomy) {
    getHibernateTemplate()
        .execute(
            session -> {
              Query query =
                  session.createQuery(
                      "delete from TermAttributes ta where ta.term.id in (from Term where taxonomy = :taxonomy)");
              query.setParameter("taxonomy", taxonomy);
              query.executeUpdate();
              query = session.createQuery("delete from Term where taxonomy = :taxonomy");
              query.setParameter("taxonomy", taxonomy);
              query.executeUpdate();
              return null;
            });
  }

  /**
   * Invalidates the full value of the given term and all of its children. It is expected that
   * <code>updateFullValues()</code> is executed after any invocations of this method and before the
   * end of the transaction.
   */
  private void invalidateFullValues(final Term term) {
    getHibernateTemplate()
        .execute(
            session -> {
              // Put a marker on full values for this term, and it's children.
              // We can't just NULL out full values, because they have a
              // uniqueness constraint we still need to abide by.
              Query q =
                  session.createQuery(
                      "UPDATE Term SET fullValue = NULL"
                          + " WHERE taxonomy = :taxonomy AND lft >= :left AND rht <= :right");
              q.setParameter("taxonomy", term.getTaxonomy());
              q.setInteger("left", term.getLeft());
              q.setInteger("right", term.getRight());
              q.executeUpdate();
              return null;
            });
  }

  private void updateFullValues(final Taxonomy taxonomy) {
    getHibernateTemplate()
        .execute(
            session -> {
              boolean foundInvalid = false;
              do {
                Query ru =
                    session.createQuery(
                        "UPDATE Term"
                            + " SET fullValue = value"
                            + " WHERE parent IS NULL AND fullValue IS NULL"
                            + " AND taxonomy = :taxonomy");
                ru.setParameter("taxonomy", taxonomy);
                ru.executeUpdate();

                Query q =
                    session.createQuery(
                        "SELECT DISTINCT(parent.id), parent.fullValue"
                            + " FROM Term WHERE fullValue IS NULL AND parent.fullValue IS NOT NULL"
                            + " AND taxonomy = :taxonomy");
                q.setParameter("taxonomy", taxonomy);

                final List<Object[]> parents = q.list();
                foundInvalid = !Check.isEmpty(parents);

                for (Object[] parent : parents) {
                  Query u =
                      session.createQuery(
                          "UPDATE Term"
                              + " SET fullValue = :parentFullValue || :termSeparator || value"
                              + " WHERE parent.id = :parentId AND fullValue IS NULL");
                  u.setString("termSeparator", TaxonomyConstants.TERM_SEPARATOR);
                  u.setString("parentFullValue", (String) parent[1]);
                  u.setLong("parentId", (Long) parent[0]);
                  u.executeUpdate();
                }
              } while (foundInvalid);

              return null;
            });
  }

  private void shift(Session session, final Taxonomy taxonomy, final Term term, final int amount) {
    if (session != null) {
      Query q = session.getNamedQuery("shiftByPath");
      q.setInteger("amount", amount);
      q.setString("fullValue", term.getFullValue());
      q.setString("fullValueWild", term.getFullValue() + "%");
      q.setParameter("taxonomy", taxonomy);
      q.executeUpdate();
    } else {
      getHibernateTemplate()
          .execute(
              newSession -> {
                shift(newSession, taxonomy, term, amount);
                return null;
              });
    }
  }

  @Override
  public void validateTerm(Taxonomy taxonomy, Term parent, String termValue) {
    checkTermValue(termValue, taxonomy, parent);
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void sortChildren(final Taxonomy taxonomy, final Term parentTerm) {
    final List<Term> children =
        (parentTerm == null ? getRootTerms(taxonomy) : getChildTerms(parentTerm));

    // track all the swaps that happened (from index to to index)
    // This should mean that the number of DB calls is <= N rather than <= N^2
    final List<TermSortData> sortData =
        new ArrayList<>(
            Lists.transform(children, (t) -> new TermSortData(t, t.getLeft(), t.getRight())));

    // Sort using server locale (in the absence of anything better!)
    // We should probably have selectable default locales on institutions (new feature!)
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);

    // Don't use sortData.sort, we specifically need bubble sort as we need to catch each swap
    // of position
    final int n = sortData.size();
    for (int i = 0; i < n; i++) {
      for (int j = 1; j < (n - i); j++) {
        final TermSortData o1 = sortData.get(j - 1);
        final TermSortData o2 = sortData.get(j);
        if (collator.compare(o1.term.getValue(), o2.term.getValue()) > 0) {
          sortData.set(j - 1, o2);
          sortData.set(j, o1);
          o1.swapPosition(o2);
        }
      }
    }

    // now perform a re-number for each modification
    getHibernateTemplate()
        .execute(
            session -> {
              for (TermSortData tsd : sortData) {
                final int shiftAmount = tsd.getShiftAmount();
                if (shiftAmount != 0) {
                  // we need to use the term path for hierarchical identification, as the
                  // left+right indexes will get messed up during this process
                  shift(session, taxonomy, tsd.term, shiftAmount);
                }
              }
              return null;
            });
  }

  private void checkTermValue(String termValue, Taxonomy taxonomy, Term parentTerm) {
    final List<ValidationError> errors = new ArrayList<ValidationError>();

    if (Check.isEmpty(termValue)) {
      errors.add(new ValidationError("term", resources.getString("dao.term.validation.emptyterm")));
    } else if (termValue.length() > Term.MAX_TERM_VALUE_LENGTH) {
      errors.add(
          new ValidationError(
              "term",
              resources.getString("dao.term.validation.termlength", Term.MAX_TERM_VALUE_LENGTH)));
    }

    if (termValue.contains(TaxonomyConstants.TERM_SEPARATOR)) {
      errors.add(
          new ValidationError(
              "term",
              resources.getString("dao.term.validation.nosep", TaxonomyConstants.TERM_SEPARATOR)));
    }

    // We need to check for term duplicate with siblings. The database
    // constraint will ensure that this is correct for all terms that are
    // not at the root level, but due to parent_id being NULL for root
    // terms, the constraint will not necessarily be enforced. We do a
    // manual check here for when this is a root term (parentTerm is null).
    if (parentTerm == null) {
      if (getRootTermValues(taxonomy).contains(termValue)) {
        errors.add(
            new ValidationError(
                "term", resources.getString("dao.term.validation.duplicate", termValue)));
      }
    } else {
      final String fullPath =
          parentTerm.getFullValue() + TaxonomyConstants.TERM_SEPARATOR + termValue;
      if (fullPath.length() > Term.MAX_TERM_FULLVALUE_LENGTH) {
        errors.add(
            new ValidationError(
                "termFullPath",
                resources.getString(
                    "dao.term.validation.termfulllength", Term.MAX_TERM_FULLVALUE_LENGTH)));
      }
    }

    if (!errors.isEmpty()) {
      throw new InvalidDataException(errors);
    }
  }

  private void shiftLeftAndRightIndexes(
      Session session, final Taxonomy taxonomy, final int from, final int to, final int amount) {
    if (session != null) {
      modifyLeftOrRight(session, "shiftLeftIndex", taxonomy, from, to, amount);
      modifyLeftOrRight(session, "shiftRightIndex", taxonomy, from, to, amount);
    } else {
      getHibernateTemplate()
          .execute(
              newSession -> {
                shiftLeftAndRightIndexes(newSession, taxonomy, from, to, amount);
                return null;
              });
    }
  }

  private void modifyLeftOrRight(
      Session session, String queryName, Taxonomy taxonomy, int from, int to, int amount) {
    Query q = session.getNamedQuery(queryName);
    q.setInteger("amount", amount);
    q.setInteger("from", from);
    q.setInteger("to", to);
    q.setParameter("taxonomy", taxonomy);
    q.executeUpdate();
  }

  private class TermSortData {
    private final Term term;
    private final int width;
    private int left;

    public TermSortData(Term term, int left, int right) {
      this.term = term;
      this.left = left;
      this.width = right - left;
    }

    public void swapPosition(TermSortData other) {
      final TermSortData oldBefore = (other.left > left ? this : other);
      final TermSortData oldAfter = (other.left > left ? other : this);

      oldAfter.left -= (oldBefore.width + 1);
      oldBefore.left += (oldAfter.width + 1);
    }

    public int getShiftAmount() {
      return left - term.getLeft();
    }
  }
}

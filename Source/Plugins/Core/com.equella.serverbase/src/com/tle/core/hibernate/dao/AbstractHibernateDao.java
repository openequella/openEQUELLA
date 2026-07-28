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

package com.tle.core.hibernate.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.hibernate.HibernateService;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

@NonNullByDefault
public abstract class AbstractHibernateDao {
  @Inject private HibernateService hibernateService;

  private SessionFactory lastFactory;

  private HibernateTemplate template;

  /**
   * Query the database using an EntityManager.
   *
   * <p>WARNING: the EntityManager supplied to {@code queryFn} has its <em>own</em> persistence
   * context - see {@link #createEntityManager(Session)} - so entities it returns are detached from
   * the session bound to the request. Two consequences: their lazy associations cannot be
   * initialised by the caller, and a caller which subsequently loads the same row (for example by
   * its concrete subclass) re-reads it from the database rather than hitting the first level cache.
   * Prefer {@link #criteriaQuery(Session, Class, BiFunction)} or {@link #findUnique(Class,
   * BiFunction)}, which build the same criteria queries on the bound session; this method is best
   * reserved for projections such as counts, where no entity is returned.
   *
   * @param queryFn A function that takes an EntityManager and returns a result
   * @return The result of the query
   */
  protected <T> T queryWithEntityManager(Function<EntityManager, T> queryFn) {
    return getHibernateTemplate()
        .execute(
            session -> {
              EntityManager entityManager = createEntityManager(session);
              return queryFn.apply(entityManager);
            });
  }

  /**
   * Query the database using a Hibernate Session.
   *
   * @param queryFn A function that takes a Session and returns a result
   * @return The result of the query
   * @param <T> The type of the result
   */
  protected <T> T withSession(Function<Session, T> queryFn) {
    return getHibernateTemplate().execute(queryFn::apply);
  }

  /**
   * Build a criteria query which selects instances of {@code entityClass} matching a predicate,
   * taking care of the builder/query/root plumbing that every such query needs.
   *
   * <p>The query is built on the supplied session, so its results participate in that session's
   * persistence context - unlike those of {@link #queryWithEntityManager(Function)}.
   *
   * <p>Use this when the query is one step of a larger piece of work already inside a {@link
   * #withSession(Function)} block, or when the results need paging, ordering, or to be returned as
   * a list. For the common "at most one match" case, {@link #findUnique(Class, BiFunction)} wraps
   * this up along with the session handling.
   *
   * <p>For example, to find the users of a group, most recently created first:
   *
   * <pre>{@code
   * withSession(
   *     session -> {
   *       Query<TLEUser> query =
   *           criteriaQuery(
   *               session,
   *               TLEUser.class,
   *               (builder, user) -> builder.equal(user.get("groupId"), groupId));
   *       return query.setMaxResults(pageSize).getResultList();
   *     });
   * }</pre>
   *
   * @param session the session to build the query on
   * @param entityClass the type of entity to select
   * @param where produces the query's restriction from the query's builder and root
   * @param <E> the type of entity to select
   * @return the query, ready to be executed or further constrained
   */
  protected <E> Query<E> criteriaQuery(
      Session session,
      Class<E> entityClass,
      BiFunction<CriteriaBuilder, Root<E>, Predicate> where) {
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<E> criteriaQuery = builder.createQuery(entityClass);
    Root<E> root = criteriaQuery.from(entityClass);

    criteriaQuery.select(root).where(where.apply(builder, root));

    return session.createQuery(criteriaQuery);
  }

  /**
   * The single result of a query, or empty if it matched nothing.
   *
   * <p>Strict: a query which matches more than one row throws {@link
   * javax.persistence.NonUniqueResultException} rather than silently returning one of them. If "any
   * one of the matches" is genuinely what is wanted, constrain the query with {@code
   * setMaxResults(1)} first, which makes that intent explicit.
   *
   * @param query the query to execute
   * @param <E> the type of the result
   * @return the sole result, or empty
   */
  protected <E> Optional<E> uniqueResult(Query<E> query) {
    return query.uniqueResultOptional();
  }

  /**
   * Find the single instance of {@code entityClass} matching a predicate, if there is one.
   *
   * <p>Combines {@link #withSession(Function)}, {@link #criteriaQuery(Session, Class, BiFunction)}
   * and {@link #uniqueResult(Query)} for the common case of a lookup expected to match at most one
   * row - typically by primary key, or by a uniquely constrained combination of columns.
   *
   * <p>For example, to find an entity by ID within the current institution:
   *
   * <pre>{@code
   * return findUnique(
   *     BaseEntity.class,
   *     (builder, entity) ->
   *         builder.and(
   *             builder.equal(entity.get("id"), id),
   *             CurrentInstitution.equalityPredicate(builder, entity)));
   * }</pre>
   *
   * @param entityClass the type of entity to find
   * @param where produces the query's restriction from the query's builder and root
   * @param <E> the type of entity to find
   * @return the matching entity, or empty if there is none
   * @throws javax.persistence.NonUniqueResultException if more than one entity matches
   */
  protected <E> Optional<E> findUnique(
      Class<E> entityClass, BiFunction<CriteriaBuilder, Root<E>, Predicate> where) {
    return withSession(session -> uniqueResult(criteriaQuery(session, entityClass, where)));
  }

  protected synchronized HibernateTemplate getHibernateTemplate() {
    SessionFactory newFactory =
        hibernateService.getTransactionAwareSessionFactory(getFactoryName(), isSystemDataSource());
    if (!newFactory.equals(lastFactory)) {
      lastFactory = newFactory;
      template =
          new HibernateTemplate(newFactory) {
            @Override
            protected Object doExecute(HibernateCallback action, boolean enforceNativeSession)
                throws DataAccessException {
              Thread currentThread = Thread.currentThread();
              ClassLoader origLoader = currentThread.getContextClassLoader();
              try {
                currentThread.setContextClassLoader(Session.class.getClassLoader());
                return super.doExecute(action, enforceNativeSession);
              } finally {
                currentThread.setContextClassLoader(origLoader);
              }
            }
          };
      template.setExposeNativeSession(true);
    }
    return template;
  }

  /**
   * Return an EntityManager to help criteria query building.
   *
   * @param session An active Hibernate Session
   */
  protected EntityManager createEntityManager(Session session) {
    return session.getEntityManagerFactory().createEntityManager();
  }

  protected boolean isSystemDataSource() {
    return false;
  }

  protected String getFactoryName() {
    return "main";
  }
}

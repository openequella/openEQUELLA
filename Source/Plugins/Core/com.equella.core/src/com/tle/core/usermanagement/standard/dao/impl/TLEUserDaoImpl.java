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

package com.tle.core.usermanagement.standard.dao.impl;

import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.CollectionPartitioner;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.usermanagement.standard.dao.TLEUserDao;
import com.tle.core.usermanagement.standard.dao.UserQueryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(TLEUserDao.class)
@Singleton
@SuppressWarnings("nls")
public class TLEUserDaoImpl extends GenericDaoImpl<TLEUser, Long> implements TLEUserDao {
  public TLEUserDaoImpl() {
    super(TLEUser.class);
  }

  @Override
  public int totalExistingUsers() {
    return Optional.ofNullable(
            getHibernateTemplate()
                .execute(
                    session -> {
                      org.hibernate.query.Query<Long> query =
                          session.createQuery(
                              "select count(*) from TLEUser where institution = :institution");
                      query.setParameter("institution", CurrentInstitution.get());

                      return query.uniqueResult();
                    }))
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public int countUsersInGroup(String likeQuery, String parentGroupID, boolean recurse) {
    return Optional.ofNullable(
            getHibernateTemplate()
                .execute(
                    session ->
                        new UserQueryBuilder<Long>()
                            .select("count(*)")
                            .withQueryString(likeQuery)
                            .withParentGroupID(parentGroupID, recurse)
                            .build(session)
                            .uniqueResult()))
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public List<TLEUser> searchUsersInGroup(String likeQuery, String parentGroupID, boolean recurse) {
    return searchUsersInGroup(likeQuery, parentGroupID, recurse, null, null);
  }

  @Override
  public List<TLEUser> searchUsersInGroup(
      String likeQuery, String parentGroupID, boolean recurse, Integer limit) {
    return searchUsersInGroup(likeQuery, parentGroupID, recurse, null, limit);
  }

  @Override
  public List<TLEUser> searchUsersInGroup(
      String userQuery,
      final String parentGroupID,
      boolean recurse,
      Integer limit,
      Integer offset) {

    return getHibernateTemplate()
        .execute(
            session -> {
              UserQueryBuilder<TLEUser> queryBuilder =
                  new UserQueryBuilder<TLEUser>()
                      .withQueryString(userQuery)
                      .withParentGroupID(parentGroupID, recurse);

              if (offset != null) {
                // If an offset is provided, we need to ensure consistent ordering
                queryBuilder.orderBy(Order.asc("id"));
              }

              org.hibernate.query.Query<TLEUser> query = queryBuilder.build(session);
              query.setCacheable(true);
              query.setReadOnly(true);
              if (offset != null) {
                query.setFirstResult(offset);
              }
              if (limit != null) {
                query.setMaxResults(limit);
              }

              return query.list();
            });
  }

  @Override
  public List<TLEUser> listAllUsers() {
    return (List<TLEUser>)
        getHibernateTemplate()
            .find("from TLEUser where institution = ?0", new Object[] {CurrentInstitution.get()});
  }

  @Override
  @SecureOnCallSystem
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteAll() {
    getHibernateTemplate().deleteAll(listAllUsers());
  }

  @Override
  public TLEUser findByUuid(final String uuid) {
    return (TLEUser)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    Query query =
                        session.createQuery(
                            "from TLEUser g where g.uuid = :uuid AND g.institution = :i");
                    query.setParameter("uuid", uuid);
                    query.setParameter("i", CurrentInstitution.get());
                    return query.uniqueResult();
                  }
                });
  }

  /**
   * It seems to have been long possible for a username to be created that was spelt the same as a
   * pre-existing one, differing only in case. This method, however, would prevent (among other
   * things) either user from logging in because the LOWER(username) query would fail by not
   * returning a unique result. From which we may conclude that there are no in-use duplicated
   * usernames in the production world
   */
  @Override
  public TLEUser findByUsername(final String username) {
    return (TLEUser)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    Query query =
                        session.createQuery(
                            "FROM TLEUser WHERE LOWER(username) = :username AND institution = :i");
                    query.setParameter("username", username.toLowerCase());
                    query.setParameter("i", CurrentInstitution.get());
                    return query.uniqueResult();
                  }
                });
  }

  /**
   * In determining that a candidate username may already be 'taken', we want a case-insensitive
   * match, and exclude the possibility that an existing user will be compared to itself (by
   * providing for unique uuid where available to be part of the query). We don't enforce a unique
   * result here - it is enough that we can identify any pre-existing takers. Should any multiple of
   * usernames exist, they would all be unworkable.
   */
  @Override
  public boolean doesOtherUsernameSameSpellingExist(final String username, final String userUuid) {
    List<?> existingUsersList =
        (List<?>)
            getHibernateTemplate()
                .execute(
                    new HibernateCallback() {
                      @Override
                      public Object doInHibernate(Session session) {
                        String queryString =
                            "FROM TLEUser WHERE LOWER(username) = :username AND institution = :i ";
                        if (!Check.isEmpty(userUuid)) {
                          queryString += " AND NOT uuid = :uuid";
                        }

                        Query query = session.createQuery(queryString);
                        query.setParameter("username", username.toLowerCase());
                        query.setParameter("i", CurrentInstitution.get());
                        if (!Check.isEmpty(userUuid)) {
                          query.setParameter("uuid", userUuid);
                        }
                        return query.list();
                      }
                    });
    return !Check.isEmpty(existingUsersList);
  }

  @Override
  public List<TLEUser> getInformationForUsers(Collection<String> ids) {
    if (Check.isEmpty(ids)) {
      return new ArrayList<TLEUser>();
    }

    return (List<TLEUser>)
        getHibernateTemplate()
            .execute(
                new CollectionPartitioner<String, TLEUser>(ids) {
                  @Override
                  public List<TLEUser> doQuery(Session session, Collection<String> collection) {
                    return session
                        .createQuery("FROM TLEUser u WHERE u.uuid in (:ids) AND u.institution = :i")
                        .setParameterList("ids", collection)
                        .setParameter("i", CurrentInstitution.get())
                        .list();
                  }
                });
  }
}

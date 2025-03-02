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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.Pagination;
import com.tle.core.dao.impl.AbstractTreeDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.AssociationCountQueryBuilder;
import com.tle.core.usermanagement.standard.dao.TLEGroupDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(TLEGroupDao.class)
@Singleton
@SuppressWarnings("nls")
public class TLEGroupDaoImpl extends AbstractTreeDaoImpl<TLEGroup> implements TLEGroupDao {
  public TLEGroupDaoImpl() {
    super(TLEGroup.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.dao.user.TLEGroupDao#getGroupsContainingUser(java.lang.String
   * )
   */
  @Override
  public List<TLEGroup> getGroupsContainingUser(String userID) {
    Preconditions.checkNotNull(userID);

    return (List<TLEGroup>)
        getHibernateTemplate()
            .findByNamedParam(
                "from TLEGroup g join g.users u where u = :userID and g.institution = :institution",
                new String[] {"userID", "institution"},
                new Object[] {userID, CurrentInstitution.get()});
  }

  @Override
  public List<TLEGroup> listAllGroups() {
    return (List<TLEGroup>) findByCriteria(CurrentInstitution.equalityCriteria());
  }

  @Override
  public List<String> getUsersInGroup(String parentGroupID, boolean includeSubGroups) {
    return getUsersInGroup(parentGroupID, includeSubGroups, null);
  }

  @Override
  public List<String> getUsersInGroup(
      String parentGroupID, boolean includeSubGroups, Pagination pagination) {
    final TLEGroup parentGroup =
        Optional.ofNullable(findByUuid(parentGroupID))
            .orElseThrow(
                () -> new NotFoundException("Group with id of " + parentGroupID + " not found."));

    return withSession(
        session -> {
          org.hibernate.query.Query<String> q =
              new UsersInGroupQuery(parentGroup).includeSubGroups(includeSubGroups).build(session);
          Optional.ofNullable(pagination)
              .ifPresent(
                  p -> {
                    p.getOffset().ifPresent(q::setFirstResult);
                    p.getLimit().ifPresent(q::setMaxResults);
                  });

          return q.list();
        });
  }

  @Transactional(propagation = Propagation.MANDATORY)
  @Override
  public boolean addUserToGroup(final String groupUuid, final String userUuid) {
    return (boolean)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query query =
                        session.createSQLQuery(
                            "SELECT id FROM tlegroup WHERE uuid = :groupUuid AND institution_id ="
                                + " :institutionId");
                    query.setParameter("groupUuid", groupUuid);
                    query.setParameter("institutionId", CurrentInstitution.get().getDatabaseId());
                    final Number groupId = (Number) query.uniqueResult();

                    query =
                        session.createSQLQuery(
                            "SELECT COUNT(*) FROM tlegroup_users WHERE tlegroup_id = :groupId AND"
                                + " element = :userId");
                    query.setParameter("groupId", groupId);
                    query.setParameter("userId", userUuid);
                    final Number count = (Number) query.uniqueResult();
                    if (count.longValue() == 0) {
                      query =
                          session.createSQLQuery(
                              "INSERT INTO tlegroup_users (tlegroup_id, element) VALUES (:groupId,"
                                  + " :userId)");
                      query.setParameter("groupId", groupId);
                      query.setParameter("userId", userUuid);
                      query.executeUpdate();
                      session.clear();
                      return true;
                    }
                    return false;
                  }
                });
  }

  @Transactional(propagation = Propagation.MANDATORY)
  @Override
  public boolean removeUserFromGroup(final String groupUuid, final String userUuid) {
    return (boolean)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query query =
                        session.createSQLQuery(
                            "SELECT id FROM tlegroup WHERE uuid = :groupUuid AND institution_id ="
                                + " :institutionId");
                    query.setParameter("groupUuid", groupUuid);
                    query.setParameter("institutionId", CurrentInstitution.get().getDatabaseId());
                    final Number groupId = (Number) query.uniqueResult();

                    query =
                        session.createSQLQuery(
                            "DELETE FROM tlegroup_users WHERE tlegroup_id = :groupId AND element ="
                                + " :userId");
                    query.setParameter("groupId", groupId);
                    query.setParameter("userId", userUuid);
                    final int rows = query.executeUpdate();
                    session.clear();
                    return rows > 0;
                  }
                });
  }

  @Override
  public TLEGroup findByUuid(final String uuid) {
    return findByCriteria(Restrictions.eq("uuid", uuid), CurrentInstitution.equalityCriteria());
  }

  @Override
  public List<TLEGroup> getInformationForGroups(Collection<String> groups) {
    if (Check.isEmpty(groups)) {
      return new ArrayList<TLEGroup>();
    }
    return (List<TLEGroup>)
        getHibernateTemplate()
            .findByNamedParam(
                "from TLEGroup g where g.uuid in (:ids) and g.institution = :institution",
                new String[] {"ids", "institution"},
                new Object[] {groups, CurrentInstitution.get()});
  }

  @Override
  public List<TLEGroup> searchGroups(String query, String parentId) {
    query = query.replace('*', '%');
    final TLEGroup parentGroup = findByUuid(parentId);
    if (parentGroup != null) {
      StringBuilder q = new StringBuilder("FROM TLEGroup g ");
      q.append(
          "WHERE g.institution = :institution AND g.name LIKE :namequery AND :parent IN"
              + " ELEMENTS(g.allParents)");

      return (List<TLEGroup>)
          getHibernateTemplate()
              .findByNamedParam(
                  q.toString(),
                  new String[] {
                    "institution", "namequery", "parent",
                  },
                  new Object[] {
                    CurrentInstitution.get(), query, parentGroup,
                  });
    }
    return Lists.newArrayList();
  }

  public long countUsersInGroup(String groupId) {
    return Optional.ofNullable(groupId)
        .flatMap(
            id -> countWithHibernate(entityManager -> buildCountGroupUsersQuery(entityManager, id)))
        .orElse(0L);
  }

  private Optional<Long> countWithHibernate(
      Function<EntityManager, TypedQuery<Long>> queryBuilderFn) {
    return Optional.ofNullable(
        queryWithEntityManager(
            entityManager -> queryBuilderFn.apply(entityManager).getSingleResult()));
  }

  private TypedQuery<Long> buildCountGroupUsersQuery(EntityManager entityManager, String groupId) {
    return new AssociationCountQueryBuilder<TLEGroup>(entityManager)
        .forEntity(TLEGroup.class)
        .forAssociation("users")
        .withId("uuid", groupId)
        .build();
  }

  private static class UsersInGroupQuery {
    private static final String paramInstitution = "institution";
    private static final String paramGroup = "group";
    private static final String aliasParent = "parent";

    private final TLEGroup group;

    private Boolean includeSubGroups = false;

    public UsersInGroupQuery(TLEGroup group) {
      this.group = group;
    }

    public UsersInGroupQuery includeSubGroups(Boolean includeSubGroups) {
      this.includeSubGroups = includeSubGroups;
      return this;
    }

    public org.hibernate.query.Query<String> build(Session session) {
      String qs = buildQueryString();

      org.hibernate.query.Query<String> query = session.createQuery(qs, String.class);
      query.setParameter(paramInstitution, CurrentInstitution.get());
      query.setParameter(paramGroup, group);

      return query;
    }

    private String buildQueryString() {
      return "SELECT u FROM TLEGroup g JOIN g.users u "
          + (includeSubGroups ? subgroupQuery() : basicQuery())
          + " ORDER BY u ASC"; // Ordering for reliable pagination
    }

    private String basicQuery() {
      return whereStatement() + " g = " + namedParam(paramGroup);
    }

    private String subgroupQuery() {
      return "LEFT OUTER JOIN g.allParents "
          + aliasParent
          + " "
          + whereStatement()
          + " (g = "
          + namedParam(paramGroup)
          + " OR "
          + aliasParent
          + " = "
          + namedParam(paramGroup)
          + ")";
    }

    private String whereStatement() {
      return "WHERE g.institution = " + namedParam(paramInstitution) + " AND";
    }

    private String namedParam(String param) {
      return ":" + param;
    }
  }
}

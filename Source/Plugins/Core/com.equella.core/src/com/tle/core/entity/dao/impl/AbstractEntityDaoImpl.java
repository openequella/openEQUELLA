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

package com.tle.core.entity.dao.impl;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.EnumerateOptions;
import com.tle.core.entity.dao.AbstractEntityDao;
import com.tle.core.entity.dao.EntityLockingDao;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.hibernate.type.HibernateEscapedString;
import java.util.*;
import javax.inject.Inject;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
public abstract class AbstractEntityDaoImpl<T extends BaseEntity>
    extends GenericInstitionalDaoImpl<T, Long> implements AbstractEntityDao<T> {
  @Inject private EntityLockingDao entityLockingDao;

  public AbstractEntityDaoImpl(Class<T> persistentClass) {
    super(persistentClass);
  }

  @Override
  public List<BaseEntityLabel> listAll(String resolveVirtualTo) {
    return listAll(resolveVirtualTo, null, false);
  }

  protected List<BaseEntityLabel> listAll(String resolveVirtualTo, final ListCallback callback) {
    return listAll(resolveVirtualTo, callback, false);
  }

  /**
   * NOTE: this and {@link #countAll(EnumerateOptions)} keep the untyped callback style, because
   * {@code ListCallback.processQuery} takes the legacy {@code org.hibernate.Query} and {@code
   * createEnumerateQuery} returns one. A typed {@code Query<R>} is a supertype of that, so it
   * cannot be handed to either without retyping both in {@code GenericInstitionalDaoImpl}.
   */
  protected List<BaseEntityLabel> listAll(
      String resolveVirtualTo, final ListCallback callback, final boolean includeSystem) {
    List<BaseEntityLabel> labels =
        (List<BaseEntityLabel>)
            getHibernateTemplate()
                .execute(
                    new TLEHibernateCallback() {
                      @Override
                      public Object doInHibernate(Session session) throws HibernateException {
                        // NOTE: Don't order by name here - use the sorting on
                        // DynamicHtmlListModel
                        StringBuilder hql = new StringBuilder();
                        hql.append("SELECT ");
                        if (callback != null && callback.isDistinct()) {
                          hql.append("DISTINCT ");
                        }
                        hql.append("NEW com.tle.beans.entity.BaseEntityLabel");
                        hql.append("(be.id, be.uuid, be.name.id, be.owner) FROM ");
                        hql.append(getPersistentClass().getName());
                        hql.append(" be ");
                        if (callback != null && !Check.isEmpty(callback.getAdditionalJoins())) {
                          hql.append(" ");
                          hql.append(callback.getAdditionalJoins());
                          hql.append(" ");
                        }
                        hql.append("WHERE be.institution = :institution");

                        if (!includeSystem) {
                          hql.append(" AND be.systemType = false");
                        }

                        if (callback != null && !Check.isEmpty(callback.getAdditionalWhere())) {
                          hql.append(" AND ");
                          hql.append(callback.getAdditionalWhere());
                        }

                        Query query = session.createQuery(hql.toString());
                        query.setParameter("institution", CurrentInstitution.get());
                        query.setCacheable(true);

                        if (callback != null) {
                          callback.processQuery(query);
                        }

                        return query.list();
                      }
                    });

    if (resolveVirtualTo != null && labels != null) {
      boolean isCollectionType = BaseEntityLabel.isCollectionType(resolveVirtualTo);
      labels.forEach(label -> label.setForCollection(isCollectionType));
    }

    return labels;
  }

  @Override
  public List<BaseEntityLabel> listEnabled(final String resolveVirtualTo) {
    return listAll(resolveVirtualTo, new EnabledCallback(null), false);
  }

  @Override
  public List<BaseEntityLabel> listAllIncludingSystem(final String resolveVirtualTo) {
    return listAll(resolveVirtualTo, null, true);
  }

  @Override
  public void delete(T entity) {
    entityLockingDao.deleteForEntity(entity);
    super.delete(entity);
  }

  @Override
  public List<T> getByIds(Collection<Long> ids) {
    // An empty "in" clause is not valid SQL on every dialect, so never build one.
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }

    return findAllByCriteria(Restrictions.in("id", ids), CurrentInstitution.equalityCriteria());
  }

  @Override
  public List<T> getByUuids(Collection<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }

    return findAllByCriteria(Restrictions.in("uuid", ids), CurrentInstitution.equalityCriteria());
  }

  @Override
  public T getByUuid(String uuid) {
    // At most one match: BaseEntity is uniquely constrained on (institution, uuid).
    return findByCriteria(Restrictions.eq("uuid", uuid), CurrentInstitution.equalityCriteria());
  }

  @Override
  public Set<String> getReferencedUsers() {
    List<String> owners =
        withSession(
            session ->
                session
                    .createQuery(
                        "SELECT DISTINCT be.owner FROM "
                            + getPersistentClass().getName()
                            + " be WHERE be.institution = :institution",
                        String.class)
                    .setParameter("institution", CurrentInstitution.get())
                    .getResultList());

    return new HashSet<>(owners);
  }

  public void setEntityLockingDao(EntityLockingDao entityLockingDao) {
    this.entityLockingDao = entityLockingDao;
  }

  @Override
  @Transactional
  public List<T> enumerateAll() {
    return enumerateAll((ListCallback) null);
  }

  @Override
  @Transactional
  public List<T> enumerateAll(EnumerateOptions options) {
    ListCallback callback = createListCallback(options);
    return super.enumerateAll(callback);
  }

  @Override
  public long countAll(EnumerateOptions options) {
    return (Long)
        getHibernateTemplate()
            .execute(
                session ->
                    super.createEnumerateQuery(session, true, createListCallback(options))
                        .uniqueResult());
  }

  private ListCallback createListCallback(EnumerateOptions options) {
    ListCallback callback = null;
    if (options != null) {
      callback = getSearchListCallback(callback, options);
      Boolean includeSystem = options.isIncludeSystem();
      if (includeSystem != null) {
        callback = new SystemCallback(callback, options.isIncludeSystem());
      }
    }
    return callback;
  }

  @Override
  protected List<T> enumerateAll(ListCallback callback) {
    return super.enumerateAll(new SystemCallback(callback, false));
  }

  @Override
  @Transactional
  public List<T> enumerateEnabled() {
    return super.enumerateAll(new EnabledCallback(null));
  }

  @Override
  @Transactional
  public List<Long> enumerateAllIds() {
    return enumerateAllIds(false);
  }

  @Override
  @Transactional
  public List<Long> enumerateAllIdsIncludingSystem() {
    return enumerateAllIds(true);
  }

  protected List<Long> enumerateAllIds(final boolean includeSystem) {
    // NOTE: Don't order by name here - use NumberStringComparator on the returned list.
    String hql =
        "SELECT be.id FROM "
            + getPersistentClass().getName()
            + " be WHERE be.institution = :institution"
            + (includeSystem ? "" : " AND be.systemType = false");

    return withSession(
        session ->
            session
                .createQuery(hql, Long.class)
                .setParameter("institution", CurrentInstitution.get())
                .getResultList());
  }

  @Override
  public String getUuidForId(final long id) {
    return withSession(
            session ->
                session
                    .createQuery(
                        "SELECT be.uuid FROM "
                            + getPersistentClass().getName()
                            + " be WHERE be.institution = :institution AND be.id = :id",
                        String.class)
                    .setParameter("institution", CurrentInstitution.get())
                    .setParameter("id", id)
                    .uniqueResultOptional())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Couldn't find entity "
                        + getPersistentClass().getName()
                        + " with ID '"
                        + id
                        + "'"));
  }

  @Override
  public List<T> search(String freetext, boolean allowArchived, int offset, int perPage) {
    return enumerateAll(
        getSearchListCallback(
            null,
            new EnumerateOptions(freetext, offset, perPage, false, allowArchived ? null : false)));
  }

  protected DefaultSearchListCallback getSearchListCallback(
      final ListCallback nestedCallback, final EnumerateOptions options) {
    ListCallback callback = nestedCallback;
    final Boolean includeDisabled = options.isIncludeDisabled();
    if (includeDisabled != null) {
      callback = new EnabledCallback(callback, !includeDisabled);
    }
    if (options.getOffset() != 0 || options.getMax() != -1) {
      callback = new PagedListCallback(callback, options.getOffset(), options.getMax());
    }
    return new DefaultSearchListCallback(callback, options.getQuery());
  }

  protected static class EnabledCallback extends BaseCallback {
    /** Tri-state value: true = enabled only, false = disabled only, null = no filter */
    private final Boolean enabled;

    public EnabledCallback(ListCallback wrappedCallback) {
      this(wrappedCallback, true);
    }

    /**
     * @param enabled Tri-state value: true = enabled only, false = disabled only, null = no filter
     */
    public EnabledCallback(ListCallback wrappedCallback, Boolean enabled) {
      super(wrappedCallback);
      this.enabled = enabled;
    }

    @Override
    public String createAdditionalWhere() {
      if (enabled != null) {
        return "be.disabled = :disabled";
      }
      return null;
    }

    @Override
    public void processQuery(Query query) {
      super.processQuery(query);
      if (enabled != null) {
        query.setParameter("disabled", !enabled);
      }
    }
  }

  protected static class SystemCallback extends BaseCallback implements ListCallback {
    private final boolean includeSystem;

    public SystemCallback(ListCallback wrappedCallback, boolean includeSystem) {
      super(wrappedCallback);
      this.includeSystem = includeSystem;
    }

    @Override
    public String createAdditionalWhere() {
      if (!includeSystem) {
        return "be.systemType = false";
      }
      return null;
    }
  }

  protected static class PagedListCallback extends BaseCallback implements ListCallback {
    protected final int offset;
    protected final int max;

    public PagedListCallback(ListCallback wrappedCallback, int offset, int max) {
      super(wrappedCallback);
      this.offset = offset;
      this.max = max;
    }

    @Override
    protected String createOrderBy() {
      // Sort the list of paged entities by their IDs.
      // Because the alias of those entity tables is 'be', we use 'be.id'.
      return "be.id";
    }

    @Override
    public void processQuery(Query query) {
      super.processQuery(query);
      if (offset > 0) {
        query.setFirstResult(offset);
      }
      if (max >= 0) {
        query.setFetchSize(max);
        query.setMaxResults(max);
      }
    }
  }

  protected static class DefaultSearchListCallback extends BaseCallback implements ListCallback {
    protected final String freetext;

    @SuppressWarnings("null")
    public DefaultSearchListCallback(ListCallback wrappedCallback, String freetext) {
      super(wrappedCallback);
      String query = freetext;
      if (query != null) {
        // remove *'s from start and end if there is one
        while (query.endsWith("*")) {
          query = query.substring(0, query.length() - 1);
        }
        while (query.startsWith("*")) {
          query = query.substring(1);
        }
        query = query.replaceAll("\\*", "%");
      }
      this.freetext = (Check.isEmpty(query) ? null : '%' + query.trim().toLowerCase() + '%');
    }

    @Override
    public String createAdditionalJoins() {
      if (freetext != null) {
        return "LEFT JOIN be.name.strings ns LEFT JOIN be.description.strings ds";
      }
      return null;
    }

    @Override
    public String createAdditionalWhere() {
      String where = null;
      if (freetext != null) {
        // CAST required for SQLServer
        where =
            "(LOWER(CAST(ns.text AS string)) LIKE :freetext "
                + "OR LOWER(CAST(ds.text AS string)) LIKE :freetext)";
      }
      return where;
    }

    @Override
    public void processQuery(Query query) {
      super.processQuery(query);
      if (freetext != null) {
        query.setParameter("freetext", freetext);
      }
    }
  }

  @Override
  public void removeOrphanedOwners(final String owner) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                Query query =
                    session.createQuery(
                        "UPDATE "
                            + getPersistentClass().getName()
                            + " SET owner = '"
                            + HibernateEscapedString.MARK_EMPTY
                            + "' WHERE owner = :owner AND institution = :institution");
                query.setParameter("owner", owner);
                query.setParameter("institution", CurrentInstitution.get());
                return query.executeUpdate();
              }
            });
  }

  @Override
  public void changeOwnerId(final String fromOwnerId, final String toOwnerId) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                Query query =
                    session.createQuery(
                        "UPDATE "
                            + getPersistentClass().getName()
                            + " SET owner = :toOwner WHERE owner = :fromOwner AND institution ="
                            + " :institution");
                query.setParameter("toOwner", toOwnerId);
                query.setParameter("fromOwner", fromOwnerId);
                query.setParameter("institution", CurrentInstitution.get());
                return query.executeUpdate();
              }
            });
  }
}

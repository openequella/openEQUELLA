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

package com.tle.core.replicatedcache.dao;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

@Bind(ReplicatedCacheDao.class)
@Singleton
@SuppressWarnings("nls")
public class ReplicatedCacheDaoImpl extends GenericDaoImpl<CachedValue, Long>
    implements ReplicatedCacheDao {
  public ReplicatedCacheDaoImpl() {
    super(CachedValue.class);
  }

  @Override
  @Transactional
  public CachedValue get(final String cacheId, final String key) {
    return (CachedValue)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query q =
                        session.createQuery(
                            "FROM CachedValue WHERE key = :key"
                                + " AND cacheId = :cacheId AND institution = :institution");
                    q.setParameter("key", key);
                    q.setParameter("cacheId", cacheId);
                    q.setParameter("institution", CurrentInstitution.get());

                    return q.uniqueResult();
                  }
                });
  }

  @Override
  @Transactional
  public CachedValue getByValue(String cacheId, byte[] value) {
    return (CachedValue)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    EntityManager entityManager = createEntityManager(session);
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    CriteriaQuery<CachedValue> criteriaQuery =
                        criteriaBuilder.createQuery(CachedValue.class);
                    Root<CachedValue> rootType = criteriaQuery.from(CachedValue.class);

                    Predicate predicateForValue =
                        criteriaBuilder.equal(
                            rootType.get("value"), Base64.getEncoder().encodeToString(value));
                    Predicate predicateForCacheId =
                        criteriaBuilder.equal(rootType.get("cacheId"), cacheId);
                    Predicate predicateForInstitution =
                        criteriaBuilder.equal(
                            rootType.get("institution"), CurrentInstitution.get());

                    Predicate finalPredicate =
                        criteriaBuilder.and(
                            predicateForValue, predicateForCacheId, predicateForInstitution);
                    criteriaQuery.where(finalPredicate);

                    return entityManager
                        .createQuery(criteriaQuery)
                        .setMaxResults(1)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                  }
                });
  }

  @Override
  @Transactional
  public void put(final String cacheId, final String key, final Date ttl, final byte[] value) {
    invalidate(cacheId, key);

    CachedValue cv = new CachedValue();
    cv.setInstitution(CurrentInstitution.get());
    cv.setCacheId(cacheId);
    cv.setKey(key);
    cv.setValue(value);
    cv.setTtl(ttl);

    save(cv);
  }

  @Override
  @Transactional
  public void invalidateExpiredEntries() {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                // Delete all the things
                Query q = session.createQuery("DELETE FROM CachedValue WHERE ttl < :ttl");
                q.setParameter("ttl", new Date());
                q.executeUpdate();

                return null;
              }
            });
  }

  @Override
  @Transactional
  public void invalidate(final String cacheId, final String... keys) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                Query q =
                    session.createQuery(
                        "DELETE FROM CachedValue WHERE cacheId = :cacheId"
                            + " AND institution = :institution AND key IN (:keys)");
                q.setParameterList("keys", Arrays.asList(keys));
                q.setParameter("cacheId", cacheId);
                q.setParameter("institution", CurrentInstitution.get());

                q.executeUpdate();
                return null;
              }
            });
  }

  @Override
  @Transactional
  public void invalidateAllForInstitution(final Institution inst) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                Query q =
                    session.createQuery("DELETE FROM CachedValue WHERE institution = :institution");
                q.setParameter("institution", inst);
                q.executeUpdate();
                return null;
              }
            });
  }

  @Override
  public Collection<CachedValue> getBatch(
      final String cacheId, final String keyPrefixFilter, final long startId, final int batchSize) {
    return (Collection<CachedValue>)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query q =
                        session.createQuery(
                            "FROM CachedValue WHERE cacheId = :cacheId AND institution ="
                                + " :institution AND id > :startId AND (:keyPrefixFilter = '' OR"
                                + " key LIKE :keyPrefixFilter) ORDER BY id ASC");
                    q.setParameter("cacheId", cacheId);
                    q.setParameter("keyPrefixFilter", keyPrefixFilter + '%');
                    q.setParameter("startId", startId);
                    q.setParameter("institution", CurrentInstitution.get());
                    q.setMaxResults(batchSize);

                    return q.list();
                  }
                });
  }
}

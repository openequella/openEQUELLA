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

package com.tle.core.portal.dao;

import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletSearch;
import java.util.List;
import javax.inject.Singleton;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;

@Singleton
@Bind(PortletDao.class)
@SuppressWarnings("nls")
public class PortletDaoImpl extends AbstractEntityDaoImpl<Portlet> implements PortletDao {
  public PortletDaoImpl() {
    super(Portlet.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Portlet> getForUser(final String userId) {
    List<Portlet> res =
        (List<Portlet>)
            getHibernateTemplate()
                .execute(
                    new HibernateCallback() {
                      @Override
                      public Object doInHibernate(Session session) throws HibernateException {
                        Query query =
                            session.createQuery(
                                "FROM Portlet WHERE (owner = :owner OR institutional ="
                                    + " :institutional) AND enabled = :enabled AND institution ="
                                    + " :institution ORDER BY dateCreated");
                        query.setCacheable(true);
                        query.setParameter("owner", userId);
                        query.setParameter("institutional", true);
                        query.setParameter("enabled", true);
                        query.setParameter("institution", CurrentInstitution.get());
                        return query.list();
                      }
                    });
    return res;
  }

  @Override
  public List<Portlet> search(PortletSearch search, int offset, int perPage) {
    return enumerateAll(
        new PortletSearchListCallback(
            search.getQuery(),
            offset,
            perPage,
            search.getOwner(),
            search.getType(),
            search.isOnlyInstWide()));
  }

  protected static class PortletSearchListCallback extends DefaultSearchListCallback {
    private final String owner;
    private final String type;
    private final Boolean onlyInstWide;

    public PortletSearchListCallback(
        String freetext, int offset, int max, String owner, String type, Boolean onlyInstWide) {
      super(new EnabledCallback(new PagedListCallback(null, offset, max), null), freetext);
      this.owner = owner;
      this.type = type;
      this.onlyInstWide = onlyInstWide;
    }

    @Override
    public String createAdditionalWhere() {
      String finalWhere = super.createAdditionalWhere();

      if (!Check.isEmpty(owner)) {
        finalWhere = concat(finalWhere, "owner = :owner", " AND ");
      }

      if (!Check.isEmpty(type)) {
        finalWhere = concat(finalWhere, "type = :type", " AND ");
      }

      if (onlyInstWide != null) {
        finalWhere = concat(finalWhere, "institutional = :institutional", " AND ");
      }

      return finalWhere;
    }

    @Override
    public void processQuery(Query query) {
      super.processQuery(query);
      if (freetext != null) {
        query.setParameter("freetext", freetext);
      }

      if (!Check.isEmpty(owner)) {
        query.setParameter("owner", owner);
      }

      if (!Check.isEmpty(type)) {
        query.setParameter("type", type);
      }

      if (onlyInstWide != null) {
        query.setParameter("institutional", onlyInstWide);
      }
    }
  }

  private long count(final ListCallback callback) {
    return ((Number)
            getHibernateTemplate()
                .execute(
                    new TLEHibernateCallback() {
                      @Override
                      public Object doInHibernate(Session session) throws HibernateException {
                        StringBuilder hql = new StringBuilder();
                        hql.append("SELECT ");
                        if (callback != null && callback.isDistinct()) {
                          hql.append("DISTINCT ");
                        }
                        hql.append("count(be) FROM ");
                        hql.append(getPersistentClass().getName());
                        hql.append(" be ");
                        if (callback != null && callback.getAdditionalJoins() != null) {
                          hql.append(" ");
                          hql.append(callback.getAdditionalJoins());
                          hql.append(" ");
                        }
                        hql.append("WHERE be.institution = :institution");

                        if (callback != null && callback.getAdditionalWhere() != null) {
                          hql.append(" AND ");
                          hql.append(callback.getAdditionalWhere());
                        }

                        Query query = session.createQuery(hql.toString());
                        query.setParameter("institution", CurrentInstitution.get());
                        query.setCacheable(true);
                        query.setReadOnly(true);

                        if (callback != null) {
                          callback.processQuery(query);
                        }
                        return query.uniqueResult();
                      }
                    }))
        .longValue();
  }

  @Override
  public long count(PortletSearch search) {
    return count(
        new PortletSearchListCallback(
            search.getQuery(),
            -1,
            -1,
            search.getOwner(),
            search.getType(),
            search.isOnlyInstWide()));
  }
}

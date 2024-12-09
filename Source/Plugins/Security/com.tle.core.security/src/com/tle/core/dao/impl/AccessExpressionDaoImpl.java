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

package com.tle.core.dao.impl;

import com.google.common.collect.Maps;
import com.tle.beans.security.AccessExpression;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@SuppressWarnings("nls")
@Bind(AccessExpressionDao.class)
public class AccessExpressionDaoImpl extends GenericDaoImpl<AccessExpression, Long>
    implements AccessExpressionDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccessExpressionDaoImpl.class);

  public AccessExpressionDaoImpl() {
    super(AccessExpression.class);
  }

  @Override
  public Long save(AccessExpression entity) {
    throw new RuntimeException("This method should not be invoked");
  }

  @Override
  public void update(AccessExpression entity) {
    throw new RuntimeException("This method should not be invoked");
  }

  @Override
  public void saveOrUpdate(AccessExpression entity) {
    throw new RuntimeException("This method should not be invoked");
  }

  @Override
  public void delete(AccessExpression entity) {
    throw new RuntimeException("This method should not be invoked");
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public AccessExpression retrieveOrCreate(String expression) {
    List<AccessExpression> list =
        (List<AccessExpression>)
            getHibernateTemplate()
                .findByNamedParam(
                    "from AccessExpression where expression = :expression",
                    "expression",
                    expression);

    AccessExpression result;
    if (!list.isEmpty()) {
      result = list.get(0);
    } else {
      result = new AccessExpression();
      result.setExpression(expression);
      result.parseExpression();
      result.setId(super.save(result));
    }
    return result;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteOrphanedExpressions() {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) {
                Query query =
                    session.createQuery(
                        "from AccessExpression expression where expression.id not in (select"
                            + " distinct entry.expression.id from AccessEntry entry)");

                int count = 0;
                for (Object obj : query.list()) {
                  session.delete(obj);
                  count++;
                }

                if (count > 0) {
                  LOGGER.info("Deleted " + count + " orphaned access expressions");
                }

                return null;
              }
            });
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public List<Triple<Long, String, Boolean>> getMatchingExpressions(final List<String> values) {
    return (List<Triple<Long, String, Boolean>>)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    // NOTE THAT THIS IS NOT HQL!!! IT IS PRETTY MUCH SQL!!!
                    String sql =
                        "SELECT id, expression, dynamic FROM access_expression WHERE id IN"
                            + " (SELECT access_expression_id FROM access_expression_expression_p"
                            + " WHERE element IN (:values))";

                    SQLQuery query = session.createSQLQuery(sql);
                    query.setParameterList("values", values);
                    query.addScalar("id", StandardBasicTypes.LONG);
                    query.addScalar("expression", StandardBasicTypes.STRING);
                    query.addScalar("dynamic", StandardBasicTypes.BOOLEAN);
                    query.setFetchSize(20);

                    List<Pair<Long, String>> results = new ArrayList<Pair<Long, String>>();

                    List<Object[]> queryResults = query.list();
                    for (Object[] o : queryResults) {
                      results.add(
                          new Triple<Long, String, Boolean>(
                              (Long) o[0], (String) o[1], (Boolean) o[2]));
                    }
                    return results;
                  }
                });
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public List<AccessExpression> listAll() {
    return (List<AccessExpression>)
        getHibernateTemplate()
            .execute(
                new TLEHibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query query = session.createQuery("from " + getPersistentClass().getName());
                    query.setCacheable(true);
                    query.setReadOnly(true);
                    return query.list();
                  }
                });
  }

  @Override
  public Map<Long, Long> userIdChanged(String fromUserId, String toUserId) {
    return changedExpression(
        SecurityConstants.getRecipient(Recipient.USER, fromUserId),
        SecurityConstants.getRecipient(Recipient.USER, toUserId));
  }

  @Override
  public Map<Long, Long> groupIdChanged(String fromGroupId, String toGroupId) {
    return changedExpression(
        SecurityConstants.getRecipient(Recipient.GROUP, fromGroupId),
        SecurityConstants.getRecipient(Recipient.GROUP, toGroupId));
  }

  private Map<Long, Long> changedExpression(final String findThis, final String replaceWithThis) {
    final Map<Long, Long> changedIds = Maps.newHashMap();
    getHibernateTemplate()
        .execute(
            new TLEHibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException {
                // NOTE THAT THIS IS NOT HQL!!! IT IS PRETTY MUCH SQL!!!
                String sql =
                    "SELECT {ae.*} FROM access_expression ae WHERE ae.id IN"
                        + " (SELECT access_expression_id FROM access_expression_expression_p"
                        + " WHERE element = :findThis)";

                SQLQuery query = session.createSQLQuery(sql);
                query.addEntity("ae", AccessExpression.class);
                query.setString("findThis", findThis);

                @SuppressWarnings("unchecked")
                Collection<AccessExpression> aes = query.list();
                for (AccessExpression ae : aes) {
                  AccessExpression newAe =
                      retrieveOrCreate(ae.getExpression().replace(findThis, replaceWithThis));
                  changedIds.put(ae.getId(), newAe.getId());
                }

                flush();

                return null;
              }
            });
    return changedIds;
  }
}

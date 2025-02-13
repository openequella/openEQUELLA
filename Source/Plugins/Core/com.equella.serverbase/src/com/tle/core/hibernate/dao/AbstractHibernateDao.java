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
import java.util.function.Function;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

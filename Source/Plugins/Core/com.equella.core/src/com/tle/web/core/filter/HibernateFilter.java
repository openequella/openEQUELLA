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

package com.tle.web.core.filter;

import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.HibernateService;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilterCallback;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Bind
@Singleton
public class HibernateFilter extends AbstractWebFilter {
  private Log LOGGER = LogFactory.getLog(HibernateFilter.class);

  @Inject private HibernateService hibernateService;

  @Override
  public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    FilterResult result = new FilterResult();
    if (CurrentInstitution.get() != null) {
      final SessionFactory sessionFactory =
          hibernateService.getTransactionAwareSessionFactory("main", false);

      if (!TransactionSynchronizationManager.hasResource(sessionFactory)) {
        LOGGER.debug("Opening single Hibernate Session in OpenSessionInViewFilter");
        Session session = openSession(sessionFactory);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        result.setCallback(
            new WebFilterCallback() {
              @Override
              public void afterServlet(HttpServletRequest request, HttpServletResponse response) {
                // single session mode
                SessionHolder sessionHolder =
                    (SessionHolder)
                        TransactionSynchronizationManager.unbindResource(sessionFactory);
                LOGGER.debug("Closing single Hibernate Session in OpenSessionInViewFilter");
                SessionFactoryUtils.closeSession(sessionHolder.getSession());
              }
            });
      }
    }
    return result;
  }

  /**
   * Prior to hibernate 5, oEQ never touched the FlushMode. Due to changes in Spring and Hibernate,
   * `openSession` was added and followed the flow of the Spring impl of `openSession`.
   *
   * <p>FlushMode is set to MANUAL to reduce surprise flushes to the database, and it'll switch to
   * AUTO in a non-read-only transaction, and then flip back.
   *
   * <p>The internal transaction handling logic with respect to flush mode appears to remain
   * unchanged
   *
   * @param sessionFactory
   * @return
   */
  private Session openSession(SessionFactory sessionFactory) {
    Session session = sessionFactory.openSession();
    session.setHibernateFlushMode(FlushMode.MANUAL);
    return session;
  }
}

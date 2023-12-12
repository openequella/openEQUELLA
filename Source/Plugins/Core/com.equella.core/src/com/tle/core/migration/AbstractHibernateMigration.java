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

package com.tle.core.migration;

import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.HibernateFactory;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import java.util.List;
import javax.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public abstract class AbstractHibernateMigration extends AbstractMigration {
  @Inject private HibernateFactoryService hibernateService;

  protected abstract Class<?>[] getDomainClasses();

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  protected HibernateMigrationHelper createMigrationHelper() {
    HibernateFactory configuration =
        hibernateService.createConfiguration(CurrentDataSource.get(), getDomainClasses());
    return new HibernateMigrationHelper(configuration);
  }

  protected void runSqlStatements(
      final List<String> sqlStatements,
      SessionFactory factory,
      final MigrationResult result,
      String statusKey) {
    result.setupSubTaskStatus(statusKey, sqlStatements.size());
    runInTransaction(factory, session -> executeSqlStatements(result, session, sqlStatements));
  }

  protected void executeSqlStatements(
      MigrationResult result, Session session, List<String> sqlStatements) {
    for (String statement : sqlStatements) {
      try {
        session.createSQLQuery(statement).executeUpdate();
        result.addLogEntry(new MigrationStatusLog(statement, false));
        result.incrementStatus();
      } catch (Exception e) {
        result.setMessage("Error running SQL: '" + statement + "' ");
        result.addLogEntry(new MigrationStatusLog(statement, true));
        throw e;
      }
    }
  }

  protected void runInTransaction(SessionFactory factory, HibernateCall call) {
    Transaction trans = null;
    try (Session session = factory.openSession()) {
      trans = session.beginTransaction();
      call.run(session);
      trans.commit();
    } catch (Exception t) {
      if (trans != null) {
        trans.rollback();
      }
      throw (t instanceof RuntimeException) ? (RuntimeException) t : new RuntimeException(t);
    }
  }

  public interface HibernateCall {
    void run(Session session) throws Exception;
  }
}

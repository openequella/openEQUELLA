/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db

import java.sql.Connection

import cats.effect.IO
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.hibernate.CurrentDataSource
import com.tle.core.hibernate.impl.HibernateServiceImpl
import com.tle.web.DebugSettings
import io.doolse.simpledba.jdbc._
import org.hibernate.jdbc.Work
import org.slf4j.LoggerFactory
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager


object RunWithDB {

  val logger = LoggerFactory.getLogger(getClass)

  lazy val getSessionFactory = HibernateServiceImpl.getInstance().getTransactionAwareSessionFactory("main", false)

  def executeWithHibernate[A](jdbc: DB[A]): A = {
    val sessionHolder = TransactionSynchronizationManager.getResource(getSessionFactory).asInstanceOf[SessionHolder]
    if (sessionHolder == null)
    {
      sys.error("There is no hibernate session - make sure it's inside @Transactional")
    }
    val con = sessionHolder.getSession().connection()
    val uc = UserContext(CurrentInstitution.get(), CurrentUser.getUserState, CurrentDataSource.get().getDataSource)
    jdbc.run(uc).runA(con).unsafeRunSync()
  }

  def executeTransaction[A](connection: Connection, jdbc: JDBCIO[A]): A = {
    if (TransactionSynchronizationManager.isSynchronizationActive) {
      val msg = "Hibernate transaction is available on this thread - should be using executeWithHibernate"
      if (DebugSettings.isDebuggingMode) sys.error(msg)
      else logger.error(msg)
    }
    jdbc.runA(connection).attempt.unsafeRunSync() match {
      case Left(e) => connection.rollback(); connection.close(); throw e
      case Right(v) => connection.commit(); connection.close(); v
    }
  }

  def executeIfInInstitution[A](db: DB[Option[A]]): Option[A] = {
    Option(CurrentInstitution.get).flatMap(_ => execute(db))
  }

  def execute[A](db: DB[A]): A = {
    val uc = UserContext(CurrentInstitution.get(), CurrentUser.getUserState, CurrentDataSource.get().getDataSource)
    executeTransaction(uc.ds.getConnection(), db.run(uc).map(a => IO.pure(a))).unsafeRunSync()
  }

  def executeWithPostCommit(db: DB[IO[Unit]]): Unit = {
    val uc = UserContext(CurrentInstitution.get(), CurrentUser.getUserState, CurrentDataSource.get().getDataSource)
    executeTransaction(uc.ds.getConnection(), db.run(uc)).unsafeRunSync()
  }

}

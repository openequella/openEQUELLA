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

package com.tle.core.hibernate.dao

import org.hibernate.Session
import org.hibernate.query.Query

import javax.persistence.NonUniqueResultException
import scala.jdk.CollectionConverters._
import scala.util.Try

/** This object provides helper functions to assist dealing with Hibernate in Scala.
  */
object DAOHelper {
  private def prepareQuery(session: Session, query: String, params: Map[String, Any]): Query[_] = {
    val namedQuery = session.getNamedQuery(query)
    params.foreach { case (key, value) =>
      namedQuery.setParameter(key, value)
    }

    namedQuery
  }

  /** This generic function is used to retrieve one entity by the provided parameters. If more than
    * one entities returned in the query result, throw an exception.
    *
    * @param dao
    *   Implementation of a DAO.
    * @param query
    *   Name of a pre-defined named query used to retrieve some entities.
    * @param params
    *   A list of parameters to be applied to the query.
    * @tparam T
    *   Type of the entity to be retrieved.
    * @tparam D
    *   Type of the DAO which MUST extend AbstractHibernateDao.
    *
    * @return
    *   Option of the only entity retrieved or None if no entity found.
    */
  def getOnlyOne[T, D <: AbstractHibernateDao](
      dao: D,
      query: String,
      params: Map[String, Any]
  ): Option[T] = {
    Try {
      dao.getHibernateTemplate
        .execute(prepareQuery(_, query, params).getResultList)
        .asScala
        .toList
        .asInstanceOf[List[T]]
    }.toEither
      .filterOrElse(
        _.size <= 1,
        new NonUniqueResultException(s"More than one entities matching the provided parameters")
      )
      .fold(throw _, _.headOption)
  }
}

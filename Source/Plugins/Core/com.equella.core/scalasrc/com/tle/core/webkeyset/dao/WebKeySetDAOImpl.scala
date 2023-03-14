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

package com.tle.core.webkeyset.dao

import com.tle.beans.webkeyset.WebKeySet
import com.tle.core.guice.Bind
import com.tle.core.hibernate.dao.GenericDaoImpl
import org.hibernate.Session
import org.hibernate.criterion.Restrictions

import javax.inject.Singleton
import scala.util.Try
import scala.jdk.CollectionConverters._

@Bind(classOf[WebKeySetDAO])
@Singleton
class WebKeySetDAOImpl
    extends GenericDaoImpl[WebKeySet, java.lang.Long](classOf[WebKeySet])
    with WebKeySetDAO {
  override def getByKeyID(keyId: String): Option[WebKeySet] =
    Try {
      getHibernateTemplate
        .execute(
          (session: Session) =>
            session
              .getNamedQuery("getByKeyID")
              .setParameter("keyId", keyId)
              .getResultList)
        .asScala
        .toList
        .asInstanceOf[List[WebKeySet]]
    }.toEither
      .filterOrElse(_.size <= 1, new Throwable(s"More than one key pairs matching key ID $keyId"))
      .fold(throw _, _.headOption)

  override def getAllByInstitution(institutionId: Long): List[WebKeySet] =
    findAllByCriteria(Restrictions.eq("inst_id", institutionId)).asScala.toList
}

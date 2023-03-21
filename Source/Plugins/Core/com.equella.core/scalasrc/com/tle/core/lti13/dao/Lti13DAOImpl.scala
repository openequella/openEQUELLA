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

package com.tle.core.lti13.dao

import com.tle.beans.lti.LtiPlatform
import com.tle.common.institution.CurrentInstitution
import com.tle.core.guice.Bind
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl
import org.hibernate.Session

import javax.inject.Singleton
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
@Bind(classOf[Lti13DAO])
class Lti13DAOImpl
    extends GenericInstitionalDaoImpl[LtiPlatform, java.lang.Long](classOf[LtiPlatform])
    with Lti13DAO {

  override def getByPlatformID(platformId: String): Option[LtiPlatform] =
    Try {
      getHibernateTemplate
        .execute(
          (session: Session) =>
            session
              .getNamedQuery("getByPlatformID")
              .setParameter("platformId", platformId)
              .setParameter("institution", CurrentInstitution.get())
              .getResultList)
        .asScala
        .toList
        .asInstanceOf[List[LtiPlatform]]
    }.toEither
      .filterOrElse(_.size <= 1,
                    new Throwable(s"More than one LTI platforms matching ID $platformId"))
      .fold(throw _, _.headOption)
}

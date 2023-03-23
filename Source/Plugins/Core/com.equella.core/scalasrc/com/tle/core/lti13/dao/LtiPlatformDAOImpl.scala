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
import com.tle.core.hibernate.dao.{DAOHelper, GenericInstitionalDaoImpl}
import javax.inject.Singleton

@Singleton
@Bind(classOf[LtiPlatformDAO])
class LtiPlatformDAOImpl
    extends GenericInstitionalDaoImpl[LtiPlatform, java.lang.Long](classOf[LtiPlatform])
    with LtiPlatformDAO {
  private def buildParams(platformId: String): Map[String, Any] =
    Map("platformId" -> platformId, "institution" -> CurrentInstitution.get())
  override def getByPlatformId(platformId: String): Option[LtiPlatform] =
    DAOHelper.getOnlyOne(this, "getByPlatformID", buildParams(platformId))

  override def deleteByPlatformId(platformId: String): true =
    DAOHelper
      .delete(this, "deleteByPlatformID", buildParams(platformId))
      .filterOrElse(_ == 1, new Throwable(s"Unexpected number of platforms have been deleted."))
      .fold(throw _, _ => true)
}

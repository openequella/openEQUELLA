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

package com.tle.core.lti13.service

import com.tle.core.guice.Bind
import com.tle.core.lti13.bean.LtiPlatformBean
import com.tle.core.lti13.dao.LtiPlatformDAO
import org.springframework.transaction.annotation.Transactional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
@Bind(classOf[LtiPlatformService])
class LtiPlatformServiceImpl extends LtiPlatformService {
  @Inject var lti13Dao: LtiPlatformDAO = _

  override def getByPlatformID(platformID: String): Either[Throwable, Option[LtiPlatformBean]] =
    Try {
      lti13Dao.getByPlatformId(platformID)
    }.map(_.map(LtiPlatformBean.apply)).toEither

  override def getAll: Either[Throwable, List[LtiPlatformBean]] =
    Try {
      lti13Dao.enumerateAll
    }.map(_.asScala.toList.map(LtiPlatformBean.apply)).toEither

  @Transactional
  override def create(bean: LtiPlatformBean): Either[Throwable, String] =
    Try {
      lti13Dao.save(bean)
    }.map(_ => bean.platformId).toEither

  @Transactional
  override def update(bean: LtiPlatformBean): Either[Throwable, Unit] =
    Try {
      lti13Dao.update(bean)
    }.toEither

  @Transactional
  override def delete(platFormId: String): Either[Throwable, true] =
    Try[true] {
      lti13Dao.deleteByPlatformId(platFormId)
    }.toEither
}

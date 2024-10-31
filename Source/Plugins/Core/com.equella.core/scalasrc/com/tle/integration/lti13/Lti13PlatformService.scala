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
package com.tle.integration.lti13

import cats.implicits._
import com.google.inject.Inject
import com.tle.core.guice.Bind
import com.tle.core.lti13.service.LtiPlatformService
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
  * Provide functionality related to the use of LTI 1.3 platforms in the integration.
  *
  */
@Bind
@Singleton
class Lti13PlatformService @Inject()(platformService: LtiPlatformService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[Lti13PlatformService])

  /**
    * Attempt to retrieve the details of an LTI 1.3 platform by platform ID. If the platform is not enabled or
    * if there isn't a platform matching the ID, return a `PlatformDetailsError`.
    */
  def getPlatform(platformId: String): Either[PlatformDetailsError, PlatformDetails] =
    platformService
      .getByPlatformID(platformId)
      .filter(_.enabled)
      .map(PlatformDetails.apply)
      .toRight {
        LOGGER.error(s"Attempt to access unauthorised platform $platformId")
        PlatformDetailsError(s"Unable to retrieve platform details of $platformId")
      }

  /**
    * Verify if the custom username claim configured for an LTI 1.3 platform is in the format of bracket notation,
    * and return a list of paths. If the verification fails, return a `PlatformDetailsError`. If the claim is not
    * configured yet, return `None`.
    */
  def verifyUsernameClaim(
      platform: PlatformDetails): Either[PlatformDetailsError, Option[List[String]]] =
    platform.usernameClaim
      .filter(_.nonEmpty)
      .map(Lti13UsernameClaimParser.parse(_).leftMap(PlatformDetailsError))
      .sequence
}

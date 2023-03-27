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

import com.tle.beans.lti.LtiPlatform
import com.tle.core.lti13.bean.LtiPlatformBean

trait LtiPlatformService {

  /**
    * Retrieve a LTI platform from the current Institution by platform ID.
    *
    * @param platformID The ID identifying a LTI platform.
    * @return Option of a LTI platform, or None if no platforms match the ID, or a throwable if failed to do so.
    */
  def getByPlatformID(platformID: String): Either[Throwable, Option[LtiPlatform]]

  /**
    * Retrieve a list of enabled or disabled LTI platforms from the current Institution.
    *
    * @param enabled whether to get enabled or disabled platforms. Default to true.
    * @return Either a list of LTI platforms or a throwable.
    */
  def getPlatforms(enabled: Boolean): Either[Throwable, List[LtiPlatform]]

  /**
    * Create a LTI platform based on the provided bean in the current Institution.
    *
    * @param bean LtiPlatformBean which provides information of a LTI platform.
    * @return platform ID of the new platform, or a throwable if failed to create.
    */
  def create(bean: LtiPlatformBean): Either[Throwable, String]

  /**
    * Update an existing LTI Platform based on the provided bean.
    *
    * @param bean LtiPlatformBean which provides updates for an existing LTI platform.
    * @return Option of Unit to indicate the successful update, or None if no LTI platform can be updated,
    *         or a throwable if failed to update.
    */
  def update(bean: LtiPlatformBean): Either[Throwable, Option[Unit]]

  /**
    * Delete a LTI platform from the current Institution by ID.
    *
    * @param platformId The ID identifying a LTI platform.
    * @return Option of Unit to indicate one platform has been deleted, or None if no such a platform can be deleted,
    *         or a throwable if failed to delete.
    */
  def delete(platformId: String): Either[Throwable, Option[Unit]]
}

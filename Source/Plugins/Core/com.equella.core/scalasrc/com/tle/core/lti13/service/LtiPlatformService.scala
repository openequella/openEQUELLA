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
    * @return Option of a LtiPlatformBean, or None if no platforms match the ID.
    */
  def getByPlatformID(platformID: String): Option[LtiPlatformBean]

  /**
    * Retrieve all the LTI platforms from the current Institution.
    *
    * @return List of LtiPlatformBean belonging to the current Institution.
    */
  def getAll: List[LtiPlatformBean]

  /**
    * Create a LTI platform based on the provided bean in the current Institution.
    *
    * @param bean LtiPlatformBean which provides information of a LTI platform.
    * @return platform ID of the new platform.
    */
  def create(bean: LtiPlatformBean): String

  /**
    * Update an existing LTI Platform based on the provided bean.
    *
    * @param bean LtiPlatformBean which provides updates for an existing LTI platform.
    * @return ID of the platform if the update is successful, or None if no such a platform can be updated.
    */
  def update(bean: LtiPlatformBean): Option[String]

  /**
    * Delete all the platforms from the current Institution.
    */
  def deleteAll: Unit

  /**
    * Delete a LTI platform from the current Institution by ID.
    *
    * @param platformId The ID identifying a LTI platform.
    * @return Option of Unit to indicate the deletion is successful, or None if no such a platform can be deleted.
    */
  def delete(platformId: String): Option[Unit]
}

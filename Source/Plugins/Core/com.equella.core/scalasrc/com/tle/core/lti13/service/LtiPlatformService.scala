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

import com.tle.core.lti13.bean.LtiPlatformBean

trait LtiPlatformService {

  /**
    * Retrieve a LTI platform from the current Institution by platform ID.
    *
    * @param platformID The ID identifying a LTI platform.
    * @return Either an Option of the LTI platform or None if no platforms match the ID, or a throwable if failed.
    */
  def getByPlatformID(platformID: String): Either[Throwable, Option[LtiPlatformBean]]

  /**
    * Retrieve all the LTI platforms for the current Institution.
    *
    * @return Either a list of LTI platforms or a throwable.
    */
  def getAll: Either[Throwable, List[LtiPlatformBean]]

  /**
    * Create a LTI platform based on the provided bean in the current Institution.
    *
    * @param bean LtiPlatformBean which provides information of a LTI platform.
    * @return Either platform ID of the new platform, or a throwable if failed to create.
    */
  def create(bean: LtiPlatformBean): Either[Throwable, String]

  /**
    * Update an existing LTI Platform based on the provided bean.
    *
    * @param bean LtiPlatformBean which provides updates for an existing LTI platform.
    * @return Either Unit to indicate the successful update, or a throwable if failed to update.
    */
  def update(bean: LtiPlatformBean): Either[Throwable, Unit]

  /**
    * Delete a LTI platform from the current Institution by ID.
    *
    * @param platformId The ID identifying a LTI platform.
    * @return Either `true` to indicate one platform has been deleted, or a throwable if failed to delete.
    */
  def delete(platformId: String): Either[Throwable, true]
}

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

trait LtiPlatformService {

  /**
    * Retrieve a LTI Platform Configuration from the current Institution by Platform ID.
    *
    * @param platformID The ID identifying a LTI Platform.
    * @return Option of the LTI platform configuration, or None if no configuration matches the ID.
    */
  def getByPlatformID(platformID: String): Option[LtiPlatform]

  /**
    * Retrieve all the LTI Platform configurations for the current Institution.
    *
    * @return A list of LTI Platform configurations.
    */
  def getAll: List[LtiPlatform]

  /**
    * Save the provided LTI Platform configuration for the current Institution.
    *
    * @param ltiPlatform The configuration to be saved.
    * @return ID of the new saved entity.
    */
  def create(ltiPlatform: LtiPlatform): Long

  /**
    * Update an existing LTI Platform.
    *
    * @param ltiPlatform The configuration that has updates.
    */
  def update(ltiPlatform: LtiPlatform): Unit

  /**
    * Delete LTI Platform configuration from the current Institution.
    *
    * @param platformID The ID identifying a LTI Platform.
    */
  def delete(ltiPlatform: LtiPlatform): Unit
}

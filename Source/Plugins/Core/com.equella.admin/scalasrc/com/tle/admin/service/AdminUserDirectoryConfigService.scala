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

package com.tle.admin.service

import com.tle.admin.usermanagement.UMPConfig
import com.tle.beans.ump.UserManagementSettings

import java.util
import java.util.Optional

/** Admin client service for user management plugin configuration.
  */
trait AdminUserDirectoryConfigService {

  /** Retrieves all shared secret IDs configured in the user management system.
    *
    * @return
    *   The list of shared secret IDs
    */
  def listSharedSecretIds: util.List[String]

  /** Saves the plugin configuration for a user management settings class.
    *
    * @param settingsClass
    *   The fully qualified class name of the settings class
    * @param settings
    *   The settings instance to be serialised and saved
    */
  def setPluginConfig(settingsClass: String, settings: UserManagementSettings): Unit

  /** Loads the plugin configuration for a user management settings class and deserialises it into
    * the settings class declared by the given plugin configuration.
    *
    * @param config
    *   the plugin configuration declaring the settings class
    * @return
    *   the deserialised settings, or an empty `Optional` if no configuration exists for the
    *   settings class
    */
  def loadSettings(config: UMPConfig): Optional[UserManagementSettings]

  /** A variant of [[loadSettings]] for use where the configuration is expected to exist.
    *
    * @return
    *   the deserialised settings
    * @throws RuntimeException
    *   if no configuration exists for the settings class
    */
  def loadSettingsOrThrow(config: UMPConfig): UserManagementSettings
}

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

package com.tle.web.remoting.graphql.provider

import com.tle.beans.ump.UserManagementSettings
import com.tle.common.security.SecurityConstants
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.services.user.UserService
import com.tle.web.remoting.graphql.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/** A Provider for operations involving user management plugin configuration. It primarily delegates
  * to `UserService`, but also handles JSON serialization/deserialization for
  * `UserManagementSettings` instances.
  */
@Bind
@Singleton
class UserDirectoryConfigProvider @Inject() (
    userService: UserService
) {
  private val LOGGER       = LoggerFactory.getLogger(classOf[UserDirectoryConfigProvider])
  private val objectMapper = new ObjectMapper()

  // Dynamically resolves a settings class by name and narrows its type to a subclass of
  // UserManagementSettings, allowing Jackson to deserialize JSON into the correct concrete type.
  private def resolveSettingsClass(settingsClassName: String): Class[_ <: UserManagementSettings] =
    Class.forName(settingsClassName).asSubclass(classOf[UserManagementSettings])

  // As above, but reported as a NotFoundError if the class cannot be resolved.
  private def resolvePluginConfigClass(
      settingsClassName: String
  ): Either[ProviderError, Class[_ <: UserManagementSettings]] =
    ProviderError
      .Try("Failed to resolve user management settings class:") {
        resolveSettingsClass(settingsClassName)
      }
      .left
      .map(_ =>
        ProviderError(
          s"User management settings class not found: $settingsClassName",
          ErrorCode.NOT_FOUND
        )
      )

  // Parses configJson into configClass and persists it. Errors are reported as a generic
  // BAD_REQUEST to avoid leaking internal implementation details (class names, field names,
  // or Jackson parser state) to the client.
  private def saveConfig(
      configClass: Class[_ <: UserManagementSettings],
      configJson: String,
      errorMessagePrefix: String
  ): Either[ProviderError, Unit] =
    ProviderError
      .Try(errorMessagePrefix + ": ") {
        val config = objectMapper.readValue(configJson, configClass)
        userService.setPluginConfig(config)
      }
      .left
      .map(_ => ProviderError(errorMessagePrefix, ErrorCode.BAD_REQUEST))

  // Get the plugin config for the given settings class and serializes it to JSON.
  private def getPluginConfig(settingsClassName: String): Either[ProviderError, String] =
    Option(userService.getPluginConfig(settingsClassName))
      .map(config => objectMapper.writeValueAsString(config))
      .toRight(
        ProviderError(
          s"User management plugin config not found for $settingsClassName",
          ErrorCode.NOT_FOUND
        )
      )

  /** Retrieves all shared secret IDs configured in the user management system.
    *
    * @return
    *   A list of shared secret IDs
    */
  @RequiresPrivilege(priv = SecurityConstants.LIST_USERS)
  def sharedSecretIds: List[String] = {
    LOGGER.debug("Retrieving shared secret IDs")

    userService.getTokenSecretIds.asScala.toList
  }

  /** Retrieves the plugin configuration for a [[UserManagementSettings]] class. The configuration
    * object is serialized to a JSON string for transport.
    *
    * @param settingsClassName
    *   The fully qualified class name of the settings class
    * @return
    *   Either a JSON string representation of the config, or a ProviderError if not found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def pluginConfig(settingsClassName: String): Either[ProviderError, String] = {
    LOGGER.debug(
      "Retrieving user management plugin config for settings class: {}",
      settingsClassName
    )

    ProviderError
      .Try(s"Failed to retrieve user management plugin config for $settingsClassName: ") {
        getPluginConfig(settingsClassName)
      }
      .flatten
  }

  /** Saves the user management plugin configuration. The provided JSON string is deserialized into
    * the appropriate UserManagementSettings object type.
    *
    * Note: Detailed error messages from JSON parsing are logged at DEBUG level but not exposed to
    * clients to avoid leaking internal implementation details (class names, field names, etc.).
    *
    * @param settingsClassName
    *   The fully qualified class name of the settings class
    * @param configJson
    *   The configuration as a JSON string to be converted to the settings object
    * @return
    *   Either a successful result, or a ProviderError. A `NotFoundError` is returned if
    *   `settingsClassName` does not exist (or is not a known settings class); otherwise a
    *   `BadRequestError` is returned if parsing or saving fails.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def setPluginConfig(
      settingsClassName: String,
      configJson: String
  ): Either[ProviderError, Unit] = {
    LOGGER.debug("Saving user management plugin config for settings class: {}", settingsClassName)
    val errorMessagePrefix = s"Failed to save user management plugin config for $settingsClassName"

    for {
      configClass <- resolvePluginConfigClass(settingsClassName)
      _           <- saveConfig(configClass, configJson, errorMessagePrefix)
    } yield ()
  }
}

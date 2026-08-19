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

import com.tle.admin.PluginServiceImpl
import com.tle.admin.helper.GraphQLQueryHelper.{executeOrThrow, getEntityOrNoneOnNotFound}
import com.tle.admin.usermanagement.UMPConfig
import com.tle.beans.ump.{UserManagementSettings, UserManagementSettingsSerialization}
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.UserDirectoryConfigApi
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/** GraphQL-backed implementation of the user management plugin configuration operations for the
  * Admin Console. JSON conversion of [[UserManagementSettings]] is delegated to the shared
  * [[com.tle.beans.ump.UserManagementSettingsSerialization]], which is also used by the server-side
  * GraphQL endpoints.
  */
@Singleton
class AdminUserDirectoryConfigServiceImpl @Inject() (pluginService: PluginServiceImpl)(implicit
    val cfg: ClientConfiguration
) extends AdminUserDirectoryConfigService {

  private implicit val LOGGER: Logger =
    LoggerFactory.getLogger(classOf[AdminUserDirectoryConfigServiceImpl])

  override def listSharedSecretIds: util.List[String] =
    executeOrThrow("listing shared secret IDs")(
      UserDirectoryConfigApi.listSharedSecretIds
    ).asJava

  override def setPluginConfig(settingsClass: String, settings: UserManagementSettings): Unit =
    setPluginConfig(settingsClass, UserManagementSettingsSerialization.toJson(settings))

  override def loadSettings(config: UMPConfig): Optional[UserManagementSettings] = {
    val settingsClassName = config.getSettingsClass
    val settingsClass     = pluginService
      .getBeanClass(config.getExtension.getDeclaringPluginDescriptor, settingsClassName)
      .asSubclass(classOf[UserManagementSettings])

    getPluginConfigByClass(settingsClassName)
      .map[UserManagementSettings](UserManagementSettingsSerialization.fromJson(_, settingsClass))
  }

  override def loadSettingsOrThrow(config: UMPConfig): UserManagementSettings =
    loadSettings(config)
      .orElseThrow(() =>
        new RuntimeException(
          s"No user management plugin config found for ${config.getSettingsClass}"
        )
      )

  // Retrieves the plugin configuration for a user management settings class.
  private def getPluginConfigByClass(settingsClass: String): Optional[String] = {
    LOGGER.debug("Retrieving user management plugin config for class: {}", settingsClass)
    getEntityOrNoneOnNotFound(
      "user management plugin config",
      settingsClass,
      UserDirectoryConfigApi.getPluginConfigByClass
    ).toJava
  }

  /** Saves the plugin configuration for a user management settings class. The provided JSON string
    * is deserialized server-side into the appropriate UserManagementSettings object type.
    */
  private def setPluginConfig(settingsClass: String, configJson: String): Unit =
    executeOrThrow(s"saving user management plugin config for: $settingsClass")(
      UserDirectoryConfigApi.setPluginConfig(settingsClass, configJson)
    )
}

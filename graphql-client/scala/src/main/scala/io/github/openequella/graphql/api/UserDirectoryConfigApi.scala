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

package io.github.openequella.graphql.api

import caliban.client.Operations.{RootMutation, RootQuery}
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.client.{
  Mutations,
  Queries,
  UserDirectoryConfigMutations,
  UserDirectoryConfigQueries
}

/** Provides access to the openEQUELLA user directory configuration API.
  */
object UserDirectoryConfigApi
    extends NestedApi[UserDirectoryConfigQueries, UserDirectoryConfigMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[UserDirectoryConfigQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.userDirectoryConfig

  override protected def mutationWrapper[A]
      : SelectionBuilder[UserDirectoryConfigMutations, A] => SelectionBuilder[RootMutation, A] =
    Mutations.userDirectoryConfig

  /** Retrieves all shared secret IDs configured in the user management system.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   A list of shared secret IDs.
    */
  def listSharedSecretIds(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[String]] = {
    val q = UserDirectoryConfigQueries.sharedSecretIds
    query(q)
  }

  /** Retrieves the plugin configuration for a specific user management settings class. The
    * configuration is returned as a JSON string.
    *
    * @param settingsClass
    *   The fully qualified class name of the settings class.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either the JSON string representation of the config, or a list of ApiError if failed to
    *   load. A `NotFoundError` is returned if `settingsClass` does not exist (or is not a known
    *   user management settings class).
    */
  def getPluginConfigByClass(settingsClass: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], String] = {
    val q = UserDirectoryConfigQueries.pluginConfig(settingsClass)
    flatQuery(q)
  }

  /** Saves the user management plugin configuration. The provided JSON string is deserialized into
    * the appropriate UserManagementSettings object type.
    *
    * @param settingsClass
    *   The fully qualified class name of the settings class.
    * @param configJson
    *   The configuration as a JSON string to be converted to the settings object.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either success(Unit), or a list of ApiError if the operation fails. A `NotFoundError` is
    *   returned if `settingsClass` does not exist (or is not a known user management settings
    *   class).
    */
  def setPluginConfig(settingsClass: String, configJson: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] = {
    val m = UserDirectoryConfigMutations.setPluginConfig(settingsClass, configJson)
    flatMutate(m)
  }
}

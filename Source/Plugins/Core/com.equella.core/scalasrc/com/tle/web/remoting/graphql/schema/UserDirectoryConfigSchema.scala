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

package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.schema.Annotations.{GQLDescription, GQLName}
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.UserDirectoryConfigProvider

import javax.inject.{Inject, Singleton}

/** The schema for user management plugin configuration GraphQL API.
  */
@Bind
@Singleton
class UserDirectoryConfigSchema @Inject() (userDirectoryConfigProvider: UserDirectoryConfigProvider)
    extends SchemaProvider {

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries,
      mutations
    )
  )

  private case class GetPluginConfigArgs(
      @GQLDescription("The fully qualified settings class name to load")
      settingsClass: String
  )

  private case class SetPluginConfigArgs(
      @GQLDescription("The fully qualified settings class name to persist")
      settingsClass: String,
      @GQLDescription("The JSON representation of the plugin configuration")
      configJson: String
  )

  @GQLName("UserDirectoryConfigQueries")
  private case class UserDirectoryConfigQueryOps(
      @GQLDescription("List the shared secret IDs used by user management plugins")
      sharedSecretIds: () => List[String],
      @GQLDescription("Retrieve a UserManagementSettings configuration as a JSON string")
      pluginConfig: GetPluginConfigArgs => ResultWithErrors[String]
  )

  @GQLName("UserDirectoryConfigMutations")
  private case class UserDirectoryConfigMutationOps(
      @GQLDescription("Persist a UserManagementSettings configuration supplied as JSON")
      setPluginConfig: SetPluginConfigArgs => ResultWithErrors[Unit]
  )

  private case class Queries(
      @GQLDescription("Queries for user management plugin configuration")
      userDirectoryConfig: UserDirectoryConfigQueryOps
  )

  private case class Mutations(
      @GQLDescription("Mutations for user management plugin configuration")
      userDirectoryConfig: UserDirectoryConfigMutationOps
  )

  private val queries = Queries(
    userDirectoryConfig = UserDirectoryConfigQueryOps(
      sharedSecretIds = () => userDirectoryConfigProvider.sharedSecretIds,
      pluginConfig = args => userDirectoryConfigProvider.pluginConfig(args.settingsClass)
    )
  )

  private val mutations = Mutations(
    userDirectoryConfig = UserDirectoryConfigMutationOps(
      setPluginConfig =
        args => userDirectoryConfigProvider.setPluginConfig(args.settingsClass, args.configJson)
    )
  )
}

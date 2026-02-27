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
import com.tle.web.remoting.graphql.provider.BaseEntityProvider
import com.tle.web.remoting.graphql.schema.types.LanguageBundle

import javax.inject.{Inject, Singleton}

/** The schema for the Base Entity GraphQL API.
  */
@Bind
@Singleton
class BaseEntitySchema extends SchemaProvider {
  private var baseEntityProvider: BaseEntityProvider = _

  /** Default constructor for Guice.
    */
  @Inject def this(baseEntityProvider: BaseEntityProvider) = {
    this()
    this.baseEntityProvider = baseEntityProvider
  }

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries
    )
  )

  case class NameByIdArgs(
      @GQLDescription("The unique ID of the base entity to retrieve")
      id: Long
  )

  @GQLName("BaseEntityQueries")
  case class BaseEntityQueryOps(
      @GQLDescription("Retrieve the name of the base entity by its unique ID")
      nameById: NameByIdArgs => Option[LanguageBundle]
  )

  case class Queries(
      @GQLDescription("Queries for base entities")
      baseEntities: BaseEntityQueryOps
  )

  private val queries = Queries(
    baseEntities = BaseEntityQueryOps(
      nameById = args => baseEntityProvider.nameById(args.id)
    )
  )
}

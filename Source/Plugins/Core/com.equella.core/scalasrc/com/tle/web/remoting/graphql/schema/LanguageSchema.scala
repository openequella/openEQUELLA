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
import com.tle.web.remoting.graphql.provider.LanguageProvider
import com.tle.web.remoting.graphql.schema.types.{Language, LanguageBundleName}

import javax.inject.{Inject, Singleton}

/** The schema for the Language GraphQL API.
  */
@Bind
@Singleton
class LanguageSchema @Inject() (languageProvider: LanguageProvider) extends SchemaProvider {

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries
    )
  )

  private val queries = Queries(
    language = LanguageQueryOps(
      list = () => languageProvider.listLanguages,
      names = args => languageProvider.namesByBundleIds(args.ids)
    )
  )

  case class Queries(
      @GQLDescription("Queries for language")
      language: LanguageQueryOps
  )

  @GQLName("LanguageQueries")
  case class LanguageQueryOps(
      @GQLDescription("List all configured languages")
      list: () => List[Language],
      @GQLDescription(
        "Resolve display text for the provided language bundle IDs. If a bundle ID cannot be resolved, it will be omitted from the results."
      )
      names: BundleNamesArgs => List[LanguageBundleName]
  )

  case class BundleNamesArgs(
      @GQLDescription("Language bundle IDs to resolve")
      ids: List[Long]
  )
}

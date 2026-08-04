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
import com.tle.web.remoting.graphql.schema.types.NameValue
import com.tle.web.remoting.graphql.provider.JavaScriptProvider

import javax.inject.{Inject, Singleton}

/** The schema for the JavaScript GraphQL API.
  */
@Bind
@Singleton
class JavaScriptSchema @Inject() (javaScriptProvider: JavaScriptProvider) extends SchemaProvider {

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries
    )
  )

  private val queries = Queries(
    javaScript = JavaScriptQueryOps(
      libraries = () => javaScriptProvider.listLibraries,
      modules = args => javaScriptProvider.modulesByLibraryId(args.id)
    )
  )

  case class Queries(
      @GQLDescription("Queries for JavaScript")
      javaScript: JavaScriptQueryOps
  )

  @GQLName("JavaScriptQueries")
  case class JavaScriptQueryOps(
      @GQLDescription("List names and IDs of all JavaScript libraries")
      libraries: () => List[NameValue],
      @GQLDescription(
        "Retrieve names and IDs of all JavaScript modules for a given JavaScript library ID"
      )
      modules: ModulesArgs => Option[List[NameValue]]
  )

  case class ModulesArgs(
      @GQLDescription(
        "The unique ID of the JavaScript library to retrieve module names and IDs for"
      )
      id: String
  )
}

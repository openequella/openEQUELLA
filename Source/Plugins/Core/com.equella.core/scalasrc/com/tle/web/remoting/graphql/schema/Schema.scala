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

import caliban.GraphQL
import com.tle.core.guice.Bind

import javax.inject.{Inject, Singleton}

trait SchemaProvider {
  def getApi: GraphQL[Any]
}

/** The main schema for the GraphQL API. This is where all the different schemas are combined into a
  * single API.
  */
@Bind
@Singleton
class Schema {
  @Inject private var tleUserSchema: TLEUserSchema   = _
  @Inject private var tleGroupSchema: TLEGroupSchema = _

  /** Get the full API for the GraphQL interface.
    */
  def getFullApi: GraphQL[Any] = {
    // NOTE: The idea here is to combine the APIs from all the different schemas using the
    // |+| operator. This is a placeholder for now.
    // See more: https://ghostdogpr.github.io/caliban/faq/#i-have-more-than-22-fields-in-my-query-i-can-t-create-a-case-class-for-it
    // Maybe we should have all API provider classes extend a common trait and then use that trait
    // to combine the APIs - by finding them all with introspection. But then the dependency injection
    // won't work. :thinking:
    tleUserSchema.getApi |+| tleGroupSchema.getApi
  }
}

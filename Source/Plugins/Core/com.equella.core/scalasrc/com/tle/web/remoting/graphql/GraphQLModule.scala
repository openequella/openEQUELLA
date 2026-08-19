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

package com.tle.web.remoting.graphql

import com.tle.core.config.guice.OptionalConfigModule
import com.tle.web.remoting.graphql.GraphQLConfig.{CFG_GRAPHQL_SCHEMA, CFG_GRAPHQL_UI}

/** List of configuration properties for the GraphQL module.
  */
object GraphQLConfig {

  /** Optional configuration property that enables the GraphQL schema endpoint.
    */
  final val CFG_GRAPHQL_SCHEMA = "graphql.schema"

  /** Optional configuration property that enables the GraphQL UI (graphiql).
    */
  final val CFG_GRAPHQL_UI = "graphql.ui"
}

/** Module for the GraphQL servlet.
  */
class GraphQLModule extends OptionalConfigModule {

  override def configure(): Unit = {
    bindBoolean(CFG_GRAPHQL_SCHEMA, false)
    bindBoolean(CFG_GRAPHQL_UI, false)
  }
}

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

package com.tle.web.remoting.graphql.schema.types

import caliban.relay.{Base64Cursor, Connection, Edge, PageInfo}
import caliban.schema.Annotations.GQLDescription
import com.tle.web.remoting.graphql.schema.Page

@GQLDescription("An Edge object for Strings as per the GraphQL Cursor Connections Specification")
final case class StringEdge(cursor: Base64Cursor, node: String) extends Edge[Base64Cursor, String]

object StringEdge {
  def apply(x: String, i: Int): StringEdge = StringEdge(Base64Cursor(i), x)
}

@GQLDescription(
  "A Connection object paging through Strings as per the GraphQL Cursor Connections Specification"
)
final case class StringConnection(pageInfo: PageInfo, edges: List[StringEdge])
    extends Connection[StringEdge]

object StringConnection {
  def apply(page: Page[String]): StringConnection =
    Page.toConnection[String, StringEdge, StringConnection](
      page,
      StringEdge.apply,
      StringConnection.apply
    )
}

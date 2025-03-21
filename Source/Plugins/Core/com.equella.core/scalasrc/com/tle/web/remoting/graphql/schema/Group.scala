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

import caliban.relay.{Base64Cursor, Connection, Edge, PageInfo}
import caliban.schema.Annotations.GQLDescription

/** The model for internal TLE Groups */
final case class Group(
    @GQLDescription("The unique identifier for the group - typically a UUID")
    uniqueId: String,
    @GQLDescription("The name of the group")
    name: String,
    @GQLDescription("The description for the group")
    description: Option[String],
    @GQLDescription(
      "The unique identifier of the parent group, or null if this is a top-level group"
    )
    parentId: Option[String],
    @GQLDescription("Whether this group has sub-groups")
    hasGroups: Boolean,
    @GQLDescription("Whether this group has users")
    hasUsers: Boolean
)

@GQLDescription("An Edge object for Groups as per the GraphQL Cursor Connections Specification")
final case class GroupEdge(cursor: Base64Cursor, node: Group) extends Edge[Base64Cursor, Group]

object GroupEdge {
  def apply(x: Group, i: Int): GroupEdge = GroupEdge(Base64Cursor(i), x)
}

@GQLDescription(
  "A Connection object paging through Groups as per the GraphQL Cursor Connections Specification"
)
final case class GroupConnection(pageInfo: PageInfo, edges: List[GroupEdge])
    extends Connection[GroupEdge]

object GroupConnection {
  def apply(page: Page[Group]): GroupConnection =
    Page.toConnection[Group, GroupEdge, GroupConnection](
      page,
      GroupEdge.apply,
      GroupConnection.apply
    )
}

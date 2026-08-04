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
import com.tle.common.usermanagement.user.valuebean.RoleBean
import com.tle.web.remoting.graphql.schema.Page

/** Represents a role sourced from the user directory system, providing a unified view of roles
  * across all configured user management plugins.
  */
@GQLDescription(
  "A role from the user directory system, representing a role from any configured user management plugin."
)
case class Role(
    @GQLDescription("The unique identifier for the role") uniqueId: String,
    @GQLDescription("The name of the role") name: String
)

object Role {
  def apply(r: RoleBean): Role = Role(r.getUniqueID, r.getName)
}

@GQLDescription("An Edge object for Roles as per the GraphQL Cursor Connections Specification")
case class RoleEdge(cursor: Base64Cursor, node: Role) extends Edge[Base64Cursor, Role]

object RoleEdge {
  def apply(x: Role, i: Int): RoleEdge = RoleEdge(Base64Cursor(i), x)
}

@GQLDescription(
  "A Connection object paging through Roles as per the GraphQL Cursor Connections Specification"
)
case class RoleConnection(pageInfo: PageInfo, edges: List[RoleEdge]) extends Connection[RoleEdge]

object RoleConnection {
  def apply(page: Page[Role]): RoleConnection =
    Page.toConnection[Role, RoleEdge, RoleConnection](
      page,
      RoleEdge.apply,
      RoleConnection.apply
    )
}

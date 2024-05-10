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

import caliban.relay.{Base64Cursor, Connection, Edge, PageInfo, Pagination}
import caliban.schema.Annotations.GQLDescription
import com.tle.beans.user.TLEUser

/**
  * A universal representation of users, for which the various user types in oEQ will be mapped to.
  */
case class User(@GQLDescription("The unique identifier for the user") uniqueId: String,
                @GQLDescription("The username a user authenticates with") username: String,
                @GQLDescription("User's email address") email: Option[String],
                @GQLDescription("User's first name") firstName: String,
                @GQLDescription("User's last name") lastName: String)

/**
  * Companion object for `User` to provide a conversion from the various oEQ user types.
  */
object User {
  def apply(u: TLEUser): User =
    User(u.getUniqueID, u.getUsername, Option(u.getEmailAddress), u.getFirstName, u.getLastName)
}

@GQLDescription("An Edge object for Users as per the GraphQL Cursor Connections Specification")
case class UserEdge(cursor: Base64Cursor, node: User) extends Edge[Base64Cursor, User]

object UserEdge {
  def apply(x: User, i: Int): UserEdge = UserEdge(Base64Cursor(i), x)
}

@GQLDescription(
  "A Connection object paging through Users as per the GraphQL Cursor Connections Specification")
case class UserConnection(pageInfo: PageInfo, edges: List[UserEdge]) extends Connection[UserEdge]

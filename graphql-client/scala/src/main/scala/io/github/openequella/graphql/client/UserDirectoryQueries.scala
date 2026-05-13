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

package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object UserDirectoryQueries {

  /** Retrieve a user by their unique ID
    */
  def userById[A](userId: String)(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "userById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("userId", userId, "String!"))
    )

  /** Retrieve multiple users by their unique IDs. Returns one entry per resolved ID; IDs not found
    * in any user directory are absent from the result.
    */
  def usersByIds[A](userIds: List[String] = Nil)(innerSelection: SelectionBuilder[UserWithId, A])(
      implicit encoder0: ArgEncoder[List[String]]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "usersByIds",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("userIds", userIds, "[String!]!"))
  )

  /** Search for users matching the query. Wildcards at the start and end of the query are implied.
    */
  def searchUsers[A](query: String)(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "searchUsers",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("query", query, "String!"))
  )

  /** Search for users within the specified group, optionally searching subgroups recursively.
    */
  def searchUsersInGroup[A](
      query: String,
      parentGroupId: scala.Option[String] = None,
      recursive: Boolean
  )(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]],
      encoder2: ArgEncoder[Boolean]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "searchUsersInGroup",
    ListOf(Obj(innerSelection)),
    arguments = List(
      Argument("query", query, "String!"),
      Argument("parentGroupId", parentGroupId, "String"),
      Argument("recursive", recursive, "Boolean!")
    )
  )

  /** Retrieve all roles assigned to the specified user
    */
  def rolesForUser[A](userId: String)(innerSelection: SelectionBuilder[Role, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "rolesForUser",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("userId", userId, "String!"))
  )

  /** Retrieve the IDs of all groups that contain the specified user
    */
  def groupIdsForUser(
      userId: String
  )(implicit encoder0: ArgEncoder[String]): SelectionBuilder[UserDirectoryQueries, List[String]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "groupIdsForUser",
      ListOf(Scalar()),
      arguments = List(Argument("userId", userId, "String!"))
    )

  /** Retrieve all groups (including subgroups) that contain the specified user
    */
  def groupsForUser[A](userId: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "groupsForUser",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("userId", userId, "String!"))
  )

  /** List all users in the specified group
    */
  def usersInGroup[A](groupId: String, recursive: Boolean)(
      innerSelection: SelectionBuilder[User, A]
  )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Boolean]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "usersInGroup",
    ListOf(Obj(innerSelection)),
    arguments =
      List(Argument("groupId", groupId, "String!"), Argument("recursive", recursive, "Boolean!"))
  )

  /** Retrieve a group by its unique ID
    */
  def groupById[A](groupId: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "groupById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("groupId", groupId, "String!"))
    )

  /** Retrieve multiple groups by their unique IDs. Returns one entry per resolved ID; IDs not found
    * in any user directory are absent from the result.
    */
  def groupsByIds[A](groupIds: List[String] = Nil)(
      innerSelection: SelectionBuilder[GroupWithId, A]
  )(implicit encoder0: ArgEncoder[List[String]]): SelectionBuilder[UserDirectoryQueries, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "groupsByIds",
      ListOf(Obj(innerSelection)),
      arguments = List(Argument("groupIds", groupIds, "[String!]!"))
    )

  /** Search for groups matching the query across the entire group hierarchy. Wildcards at the start
    * and end of the query are implied.
    */
  def searchGroups[A](query: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "searchGroups",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("query", query, "String!"))
  )

  /** Search for groups matching the query within the specified parent group. Wildcards at the start
    * and end of the query are implied.
    */
  def searchGroupsInParent[A](query: String, parentGroupId: String)(
      innerSelection: SelectionBuilder[Group, A]
  )(implicit encoder0: ArgEncoder[String]): SelectionBuilder[UserDirectoryQueries, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "searchGroupsInParent",
      ListOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String!"),
        Argument("parentGroupId", parentGroupId, "String!")
      )
    )

  /** Retrieve the parent group of the specified group, if one exists
    */
  def parentGroup[A](groupId: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "parentGroup",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("groupId", groupId, "String!"))
    )

  /** Retrieve a role by its unique ID
    */
  def roleById[A](roleId: String)(innerSelection: SelectionBuilder[Role, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "roleById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("roleId", roleId, "String!"))
    )

  /** Retrieve multiple roles by their unique IDs. Returns one entry per resolved ID; IDs not found
    * in any user directory are absent from the result.
    */
  def rolesByIds[A](roleIds: List[String] = Nil)(innerSelection: SelectionBuilder[RoleWithId, A])(
      implicit encoder0: ArgEncoder[List[String]]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "rolesByIds",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("roleIds", roleIds, "[String!]!"))
  )

  /** Search for roles matching the query. Wildcards at the start and end of the query are implied.
    */
  def searchRoles[A](query: String)(innerSelection: SelectionBuilder[Role, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[UserDirectoryQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "searchRoles",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("query", query, "String!"))
  )
}

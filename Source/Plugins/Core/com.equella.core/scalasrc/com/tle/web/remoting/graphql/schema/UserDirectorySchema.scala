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
import caliban.relay.{Base64Cursor, Pagination, PaginationArgs}
import caliban.schema.Annotations.{GQLDescription, GQLName}
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.UserDirectoryProvider
import com.tle.web.remoting.graphql.schema.types.{
  GroupConnection,
  GroupWithId,
  Role,
  RoleConnection,
  RoleWithId,
  User,
  Group,
  UserConnection,
  UserWithId
}
import zio.IO

import javax.inject.{Inject, Singleton}

/** The schema for the User Directory GraphQL API. The user directory aggregates users, groups, and
  * roles from all configured user management plugins (e.g. LDAP, Internal TLE, etc.).
  */
@Bind
@Singleton
class UserDirectorySchema @Inject() (userDirectoryProvider: UserDirectoryProvider)
    extends SchemaProvider {

  // ---------------------------------------------------------------------------
  // Argument case classes
  // ---------------------------------------------------------------------------

  private case class SearchUsersArgs(
      @GQLDescription(
        "The query to search for users by (server will surround with wildcard). Matches against username, first name, and last name."
      )
      query: String,
      @GQLDescription(
        "Pagination - how many users to return from the start of the possible list of users"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many users to return from the end of the possible list of users"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a user before which all users should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a user after which all users should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  private case class SearchUsersInGroupArgs(
      @GQLDescription(
        "The query to search for users by (server will surround with wildcard). Matches against username, first name, and last name."
      )
      query: String,
      @GQLDescription(
        "The unique ID of the parent group to restrict the search to"
      )
      parentGroupId: String,
      @GQLDescription("Whether to search subgroups recursively")
      recursive: Boolean,
      @GQLDescription(
        "Pagination - how many users to return from the start of the possible list of users"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many users to return from the end of the possible list of users"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a user before which all users should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a user after which all users should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  private case class UserByIdArgs(
      @GQLDescription("The unique ID of the user to retrieve") userId: String
  )

  private case class UsersByIdsArgs(
      @GQLDescription("The unique IDs of the users to retrieve") userIds: Set[String]
  )

  private case class RolesForUserArgs(
      @GQLDescription("The unique ID of the user") userId: String
  )

  private case class GroupIdsForUserArgs(
      @GQLDescription("The unique ID of the user") userId: String
  )

  private case class GroupsForUserArgs(
      @GQLDescription("The unique ID of the user") userId: String
  )

  private case class UsersInGroupArgs(
      @GQLDescription("The unique ID of the group") groupId: String,
      @GQLDescription("Whether to include users from subgroups recursively") recursive: Boolean,
      @GQLDescription(
        "Pagination - how many users to return from the start of the possible list of users"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many users to return from the end of the possible list of users"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a user before which all users should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a user after which all users should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  private case class GroupByIdArgs(
      @GQLDescription("The unique ID of the group to retrieve") groupId: String
  )

  private case class GroupsByIdsArgs(
      @GQLDescription("The unique IDs of the groups to retrieve") groupIds: Set[String]
  )

  private case class SearchGroupsArgs(
      @GQLDescription(
        "The query to search for groups by (server will surround with wildcard)"
      )
      query: String,
      @GQLDescription(
        "Pagination - how many groups to return from the start of the possible list of groups"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many groups to return from the end of the possible list of groups"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a group before which all groups should be returned"
      )
      before: Option[String],
      @GQLDescription(
        "Pagination - the cursor for a group after which all groups should be returned"
      )
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  private case class SearchGroupsInParentArgs(
      @GQLDescription(
        "The query to search for groups by (server will surround with wildcard)"
      )
      query: String,
      @GQLDescription("The unique ID of the parent group to restrict the search to")
      parentGroupId: String,
      @GQLDescription(
        "Pagination - how many groups to return from the start of the possible list of groups"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many groups to return from the end of the possible list of groups"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a group before which all groups should be returned"
      )
      before: Option[String],
      @GQLDescription(
        "Pagination - the cursor for a group after which all groups should be returned"
      )
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  private case class ParentGroupArgs(
      @GQLDescription("The unique ID of the group to retrieve the parent for") groupId: String
  )

  private case class RoleByIdArgs(
      @GQLDescription("The unique ID of the role to retrieve") roleId: String
  )

  private case class RolesByIdsArgs(
      @GQLDescription("The unique IDs of the roles to retrieve") roleIds: Set[String]
  )

  private case class SearchRolesArgs(
      @GQLDescription(
        "The query to search for roles by (server will surround with wildcard)"
      )
      query: String,
      @GQLDescription(
        "Pagination - how many roles to return from the start of the possible list of roles"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many roles to return from the end of the possible list of roles"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a role before which all roles should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a role after which all roles should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  // ---------------------------------------------------------------------------
  // Query operations
  // ---------------------------------------------------------------------------

  @GQLName("UserDirectoryQueries")
  @GQLDescription(
    "Queries for the user directory, which aggregates users, groups, and roles from all configured user management plugins."
  )
  private case class UserDirectoryQueryOps(
      @GQLDescription("Retrieve a user by their unique ID")
      userById: UserByIdArgs => ResultWithErrors[User],
      @GQLDescription(
        "Retrieve multiple users by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      usersByIds: UsersByIdsArgs => List[UserWithId],
      @GQLDescription(
        "Search for users matching the query. Wildcards at the start and end of the query are implied."
      )
      searchUsers: SearchUsersArgs => IO[CalibanError, UserConnection],
      @GQLDescription(
        "Search for users within the specified group, optionally searching subgroups recursively. Returns NOT_FOUND if the group cannot be resolved."
      )
      searchUsersInGroup: SearchUsersInGroupArgs => IO[CalibanError, UserConnection],
      @GQLDescription(
        "Retrieve all roles assigned to the specified user. Returns NOT_FOUND if the user cannot be resolved."
      )
      rolesForUser: RolesForUserArgs => ResultWithErrors[List[Role]],
      @GQLDescription(
        "Retrieve the IDs of all groups that contain the specified user. Returns NOT_FOUND if the user cannot be resolved."
      )
      groupIdsForUser: GroupIdsForUserArgs => ResultWithErrors[List[String]],
      @GQLDescription(
        "Retrieve all groups (including subgroups) that contain the specified user. Returns NOT_FOUND if the user cannot be resolved."
      )
      groupsForUser: GroupsForUserArgs => ResultWithErrors[List[Group]],
      @GQLDescription(
        "List all users in the specified group. Returns NOT_FOUND if the group cannot be resolved."
      )
      usersInGroup: UsersInGroupArgs => IO[CalibanError, UserConnection],
      @GQLDescription(
        "Retrieve a group by its unique ID. Returns NOT_FOUND if the group cannot be resolved."
      )
      groupById: GroupByIdArgs => ResultWithErrors[Group],
      @GQLDescription(
        "Retrieve multiple groups by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      groupsByIds: GroupsByIdsArgs => List[GroupWithId],
      @GQLDescription(
        "Search for groups matching the query across the entire group hierarchy. Wildcards at the start and end of the query are implied."
      )
      searchGroups: SearchGroupsArgs => IO[CalibanError, GroupConnection],
      @GQLDescription(
        "Search for groups matching the query within the specified parent group. Wildcards at the start and end of the query are implied. Returns NOT_FOUND if the parent group cannot be resolved."
      )
      searchGroupsInParent: SearchGroupsInParentArgs => IO[CalibanError, GroupConnection],
      @GQLDescription(
        "Retrieve the parent group of the specified group, if one exists. Returns NOT_FOUND if the group cannot be resolved."
      )
      parentGroup: ParentGroupArgs => ResultWithErrors[Option[Group]],
      @GQLDescription(
        "Retrieve a role by its unique ID. Returns NOT_FOUND if the role cannot be resolved."
      )
      roleById: RoleByIdArgs => ResultWithErrors[Role],
      @GQLDescription(
        "Retrieve multiple roles by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      rolesByIds: RolesByIdsArgs => List[RoleWithId],
      @GQLDescription(
        "Search for roles matching the query. Wildcards at the start and end of the query are implied."
      )
      searchRoles: SearchRolesArgs => IO[CalibanError, RoleConnection]
  )

  private case class Queries(
      @GQLDescription(
        "Queries for the user directory - users, groups, and roles from all configured user management plugins"
      )
      userDirectory: UserDirectoryQueryOps
  )

  // ---------------------------------------------------------------------------
  // Resolvers
  // ---------------------------------------------------------------------------

  private val queries = Queries(
    userDirectory = UserDirectoryQueryOps(
      userById = args => userDirectoryProvider.userById(args.userId),
      usersByIds = args => userDirectoryProvider.usersByIds(args.userIds),
      searchUsers = args =>
        for {
          pagination <- Pagination(args)
          result = userDirectoryProvider.searchUsers(args.query, pagination)
        } yield result,
      searchUsersInGroup = args =>
        for {
          pagination <- Pagination(args)
          result     <- userDirectoryProvider.searchUsersInGroup(
            args.query,
            args.parentGroupId,
            args.recursive,
            pagination
          )
        } yield result,
      rolesForUser = args => userDirectoryProvider.rolesForUser(args.userId),
      groupIdsForUser = args => userDirectoryProvider.groupIdsForUser(args.userId),
      groupsForUser = args => userDirectoryProvider.groupsForUser(args.userId),
      usersInGroup = args =>
        for {
          pagination <- Pagination(args)
          result     <- userDirectoryProvider.usersInGroup(
            args.groupId,
            args.recursive,
            pagination
          )
        } yield result,
      groupById = args => userDirectoryProvider.groupById(args.groupId),
      groupsByIds = args => userDirectoryProvider.groupsByIds(args.groupIds),
      searchGroups = args =>
        for {
          pagination <- Pagination(args)
          result = userDirectoryProvider.searchGroups(args.query, pagination)
        } yield result,
      searchGroupsInParent = args =>
        for {
          pagination <- Pagination(args)
          result     <- userDirectoryProvider.searchGroupsInParent(
            args.query,
            args.parentGroupId,
            pagination
          )
        } yield result,
      parentGroup = args => userDirectoryProvider.parentGroup(args.groupId),
      roleById = args => userDirectoryProvider.roleById(args.roleId),
      rolesByIds = args => userDirectoryProvider.rolesByIds(args.roleIds),
      searchRoles = args =>
        for {
          pagination <- Pagination(args)
          result = userDirectoryProvider.searchRoles(args.query, pagination)
        } yield result
    )
  )

  /** Get the API for the User Directory GraphQL schema.
    */
  override def getApi: GraphQL[Any] = graphQL(RootResolver(queries))
}

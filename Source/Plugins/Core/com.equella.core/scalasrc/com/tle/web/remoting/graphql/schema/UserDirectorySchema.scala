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
import com.tle.web.remoting.graphql.provider.UserDirectoryProvider
import com.tle.web.remoting.graphql.schema.types.{
  GroupWithId,
  Role,
  RoleWithId,
  User,
  Group,
  UserWithId
}

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
      query: String
  )

  private case class SearchUsersInGroupArgs(
      @GQLDescription(
        "The query to search for users by (server will surround with wildcard). Matches against username, first name, and last name."
      )
      query: String,
      @GQLDescription(
        "The unique ID of the parent group to restrict the search to, or none for all groups"
      )
      parentGroupId: Option[String],
      @GQLDescription("Whether to search subgroups recursively")
      recursive: Boolean
  )

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
      @GQLDescription("Whether to include users from subgroups recursively") recursive: Boolean
  )

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
      query: String
  )

  private case class SearchGroupsInParentArgs(
      @GQLDescription(
        "The query to search for groups by (server will surround with wildcard)"
      )
      query: String,
      @GQLDescription("The unique ID of the parent group to restrict the search to")
      parentGroupId: String
  )

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
      query: String
  )

  // ---------------------------------------------------------------------------
  // Query operations
  // ---------------------------------------------------------------------------

  @GQLName("UserDirectoryQueries")
  @GQLDescription(
    "Queries for the user directory, which aggregates users, groups, and roles from all configured user management plugins."
  )
  private case class UserDirectoryQueryOps(
      @GQLDescription("Retrieve a user by their unique ID")
      userById: UserByIdArgs => Option[User],
      @GQLDescription(
        "Retrieve multiple users by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      usersByIds: UsersByIdsArgs => List[UserWithId],
      @GQLDescription(
        "Search for users matching the query. Wildcards at the start and end of the query are implied."
      )
      searchUsers: SearchUsersArgs => List[User],
      @GQLDescription(
        "Search for users within the specified group, optionally searching subgroups recursively."
      )
      searchUsersInGroup: SearchUsersInGroupArgs => List[User],
      @GQLDescription("Retrieve all roles assigned to the specified user")
      rolesForUser: RolesForUserArgs => List[Role],
      @GQLDescription("Retrieve the IDs of all groups that contain the specified user")
      groupIdsForUser: GroupIdsForUserArgs => List[String],
      @GQLDescription(
        "Retrieve all groups (including subgroups) that contain the specified user"
      )
      groupsForUser: GroupsForUserArgs => List[Group],
      @GQLDescription("List all users in the specified group")
      usersInGroup: UsersInGroupArgs => List[User],
      @GQLDescription("Retrieve a group by its unique ID")
      groupById: GroupByIdArgs => Option[Group],
      @GQLDescription(
        "Retrieve multiple groups by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      groupsByIds: GroupsByIdsArgs => List[GroupWithId],
      @GQLDescription(
        "Search for groups matching the query across the entire group hierarchy. Wildcards at the start and end of the query are implied."
      )
      searchGroups: SearchGroupsArgs => List[Group],
      @GQLDescription(
        "Search for groups matching the query within the specified parent group. Wildcards at the start and end of the query are implied."
      )
      searchGroupsInParent: SearchGroupsInParentArgs => List[Group],
      @GQLDescription("Retrieve the parent group of the specified group, if one exists")
      parentGroup: ParentGroupArgs => Option[Group],
      @GQLDescription("Retrieve a role by its unique ID")
      roleById: RoleByIdArgs => Option[Role],
      @GQLDescription(
        "Retrieve multiple roles by their unique IDs. Returns one entry per resolved ID; IDs not found in any user directory are absent from the result."
      )
      rolesByIds: RolesByIdsArgs => List[RoleWithId],
      @GQLDescription(
        "Search for roles matching the query. Wildcards at the start and end of the query are implied."
      )
      searchRoles: SearchRolesArgs => List[Role]
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
      searchUsers = args => userDirectoryProvider.searchUsers(args.query),
      searchUsersInGroup = args =>
        userDirectoryProvider.searchUsersInGroup(args.query, args.parentGroupId, args.recursive),
      rolesForUser = args => userDirectoryProvider.rolesForUser(args.userId),
      groupIdsForUser = args => userDirectoryProvider.groupIdsForUser(args.userId),
      groupsForUser = args => userDirectoryProvider.groupsForUser(args.userId),
      usersInGroup = args => userDirectoryProvider.usersInGroup(args.groupId, args.recursive),
      groupById = args => userDirectoryProvider.groupById(args.groupId),
      groupsByIds = args => userDirectoryProvider.groupsByIds(args.groupIds),
      searchGroups = args => userDirectoryProvider.searchGroups(args.query),
      searchGroupsInParent =
        args => userDirectoryProvider.searchGroupsInParent(args.query, args.parentGroupId),
      parentGroup = args => userDirectoryProvider.parentGroup(args.groupId),
      roleById = args => userDirectoryProvider.roleById(args.roleId),
      rolesByIds = args => userDirectoryProvider.rolesByIds(args.roleIds),
      searchRoles = args => userDirectoryProvider.searchRoles(args.query)
    )
  )

  /** Get the API for the User Directory GraphQL schema.
    */
  override def getApi: GraphQL[Any] = graphQL(RootResolver(queries))
}

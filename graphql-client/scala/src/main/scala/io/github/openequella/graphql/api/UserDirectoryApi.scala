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

package io.github.openequella.graphql.api

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.{
  UserDirectoryGroupView,
  UserDirectoryGroupWithIdView,
  UserDirectoryRoleView,
  UserDirectoryRoleWithIdView,
  UserDirectoryUserView,
  UserDirectoryUserWithIdView
}
import io.github.openequella.graphql.client.{
  GroupConnection,
  GroupEdge,
  Queries,
  RoleConnection,
  RoleEdge,
  UserConnection,
  UserDirectoryQueries,
  UserEdge
}

/** Provides access to the openEQUELLA user directory API.
  */
object UserDirectoryApi extends NestedQueryApi[UserDirectoryQueries] {

  override protected def queryWrapper[A]
      : SelectionBuilder[UserDirectoryQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.userDirectory

  private val userConnection
      : SelectionBuilder[UserConnection, ConnectionView[UserDirectoryUserView]] =
    ConnectionView.selector[UserConnection, UserEdge, UserDirectoryUserView](
      UserConnection.pageInfo,
      UserConnection.edges,
      UserEdge.cursor,
      UserEdge.node { UserDirectoryUserView.selector }
    )

  private val groupConnection
      : SelectionBuilder[GroupConnection, ConnectionView[UserDirectoryGroupView]] =
    ConnectionView.selector[GroupConnection, GroupEdge, UserDirectoryGroupView](
      GroupConnection.pageInfo,
      GroupConnection.edges,
      GroupEdge.cursor,
      GroupEdge.node { UserDirectoryGroupView.selector }
    )

  private val roleConnection
      : SelectionBuilder[RoleConnection, ConnectionView[UserDirectoryRoleView]] =
    ConnectionView.selector[RoleConnection, RoleEdge, UserDirectoryRoleView](
      RoleConnection.pageInfo,
      RoleConnection.edges,
      RoleEdge.cursor,
      RoleEdge.node { UserDirectoryRoleView.selector }
    )

  /** Retrieves a user by unique ID.
    *
    * @param userId
    *   Unique ID of the user to retrieve.
    * @return
    *   The matching user, or a [[NotFoundError]] if the ID cannot be resolved.
    */
  def userById(userId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], UserDirectoryUserView] = {
    val q = UserDirectoryQueries.userById(userId) {
      UserDirectoryUserView.selector
    }
    flatQuery(q)
  }

  /** Retrieves users for the provided unique IDs, omitting IDs that cannot be resolved.
    *
    * @param userIds
    *   Unique IDs of users to resolve.
    * @return
    *   Resolved users paired with their requested IDs.
    */
  def usersByIds(userIds: List[String])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[UserDirectoryUserWithIdView]] = {
    val q = UserDirectoryQueries.usersByIds(userIds) {
      UserDirectoryUserWithIdView.selector
    }
    query(q)
  }

  /** Searches users by free-text query.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match users.
    * @return
    *   A page of matching users and continuation information.
    */
  def searchUsers(pagination: Pagination, searchText: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.searchUsers(searchText, first, last, before, after) {
        userConnection
      }
    }

  /** Searches users directly in a group.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match users.
    * @param groupId
    *   Unique ID of the group used to limit the search.
    * @return
    *   A page of matching users directly in the group, or a [[NotFoundError]] if the group cannot
    *   be resolved.
    * @see
    *   [[searchUsersInGroupRecursively]] to include users from subgroups.
    */
  def searchUsersInGroup(
      pagination: Pagination,
      searchText: String,
      groupId: String
  )(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    searchUsersInGroup(pagination, searchText, groupId, recursive = false)

  /** Searches users in a group and its subgroups.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match users.
    * @param groupId
    *   Unique ID of the group used to limit the search.
    * @return
    *   A page of matching users from the group and its subgroups, or a [[NotFoundError]] if the
    *   group cannot be resolved.
    * @see
    *   [[searchUsersInGroup]] to exclude users from subgroups.
    */
  def searchUsersInGroupRecursively(
      pagination: Pagination,
      searchText: String,
      groupId: String
  )(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    searchUsersInGroup(pagination, searchText, groupId, recursive = true)

  /** Lists roles assigned to a user.
    *
    * @param userId
    *   Unique ID of the user.
    * @return
    *   Roles assigned to the user, or a [[NotFoundError]] if the user cannot be resolved.
    */
  def rolesForUser(userId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[UserDirectoryRoleView]] = {
    val q = UserDirectoryQueries.rolesForUser(userId) {
      UserDirectoryRoleView.selector
    }
    flatQuery(q)
  }

  /** Lists the IDs of groups that contain the specified user. This is recursive: if a user belongs
    * to a child group, parent groups are also treated as containing the user.
    *
    * @param userId
    *   Unique ID of the user.
    * @return
    *   Unique IDs of groups containing the user, or a [[NotFoundError]] if the user cannot be
    *   resolved.
    */
  def groupIdsForUser(userId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[String]] = {
    val q = UserDirectoryQueries.groupIdsForUser(userId)
    flatQuery(q)
  }

  /** Lists groups that contain the specified user.
    *
    * @param userId
    *   Unique ID of the user.
    * @return
    *   Groups containing the user, or a [[NotFoundError]] if the user cannot be resolved.
    */
  def groupsForUser(userId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[UserDirectoryGroupView]] = {
    val q = UserDirectoryQueries.groupsForUser(userId) {
      UserDirectoryGroupView.selector
    }
    flatQuery(q)
  }

  /** Lists users directly assigned to a group.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param groupId
    *   Unique ID of the group.
    * @return
    *   A page of users directly assigned to the group, or a [[NotFoundError]] if the group cannot
    *   be resolved.
    * @see
    *   [[usersInGroupRecursively]] to include users from subgroups.
    */
  def usersInGroup(pagination: Pagination, groupId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    usersInGroup(pagination, groupId, recursive = false)

  /** Lists users assigned to a group or any of its subgroups.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param groupId
    *   Unique ID of the group.
    * @return
    *   A page of users assigned to the group or its subgroups, or a [[NotFoundError]] if the group
    *   cannot be resolved.
    * @see
    *   [[usersInGroup]] to exclude users from subgroups.
    */
  def usersInGroupRecursively(pagination: Pagination, groupId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    usersInGroup(pagination, groupId, recursive = true)

  private def usersInGroup(
      pagination: Pagination,
      groupId: String,
      recursive: Boolean
  )(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.usersInGroup(groupId, recursive, first, last, before, after) {
        userConnection
      }
    }

  /** Retrieves a group by unique ID.
    *
    * @param groupId
    *   Unique ID of the group to retrieve.
    * @return
    *   The matching group, or a [[NotFoundError]] if the ID cannot be resolved.
    */
  def groupById(groupId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], UserDirectoryGroupView] = {
    val q = UserDirectoryQueries.groupById(groupId) {
      UserDirectoryGroupView.selector
    }
    flatQuery(q)
  }

  /** Retrieves groups for the provided unique IDs, omitting IDs that cannot be resolved.
    *
    * @param groupIds
    *   Unique IDs of groups to resolve.
    * @return
    *   Resolved groups paired with their requested IDs.
    */
  def groupsByIds(groupIds: List[String])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[UserDirectoryGroupWithIdView]] = {
    val q = UserDirectoryQueries.groupsByIds(groupIds) {
      UserDirectoryGroupWithIdView.selector
    }
    query(q)
  }

  /** Searches groups by free-text query.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match groups.
    * @return
    *   A page of matching groups.
    */
  def searchGroups(pagination: Pagination, searchText: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryGroupView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.searchGroups(searchText, first, last, before, after) {
        groupConnection
      }
    }

  /** Searches groups inside a parent group.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match groups.
    * @param parentId
    *   Unique ID of the parent group used to scope results.
    * @return
    *   A page of matching child groups, or a [[NotFoundError]] if the parent group cannot be
    *   resolved.
    */
  def searchGroupsInParent(pagination: Pagination, searchText: String, parentId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryGroupView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.searchGroupsInParent(searchText, parentId, first, last, before, after) {
        groupConnection
      }
    }

  /** Retrieves the parent group of a group.
    *
    * @param groupId
    *   Unique ID of the group whose parent should be returned.
    * @return
    *   The parent group, `None` if the group has no parent, or a [[NotFoundError]] if the group
    *   cannot be resolved.
    */
  def parentGroup(groupId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[UserDirectoryGroupView]] = {
    val q = UserDirectoryQueries.parentGroup(groupId) {
      UserDirectoryGroupView.selector
    }
    query(q)
  }

  /** Retrieves a role by unique ID.
    *
    * @param roleId
    *   Unique ID of the role to retrieve.
    * @return
    *   The matching role, or a [[NotFoundError]] if the ID cannot be resolved.
    */
  def roleById(roleId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], UserDirectoryRoleView] = {
    val q = UserDirectoryQueries.roleById(roleId) {
      UserDirectoryRoleView.selector
    }
    flatQuery(q)
  }

  /** Retrieves roles for the provided unique IDs, omitting IDs that cannot be resolved.
    *
    * @param roleIds
    *   Unique IDs of roles to resolve.
    * @return
    *   Resolved roles paired with their requested IDs.
    */
  def rolesByIds(roleIds: List[String])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[UserDirectoryRoleWithIdView]] = {
    val q = UserDirectoryQueries.rolesByIds(roleIds) {
      UserDirectoryRoleWithIdView.selector
    }
    query(q)
  }

  /** Searches roles by free-text query.
    *
    * @param pagination
    *   Cursor pagination settings for the result page.
    * @param searchText
    *   Query text used to match roles.
    * @return
    *   A page of matching roles.
    */
  def searchRoles(pagination: Pagination, searchText: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryRoleView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.searchRoles(searchText, first, last, before, after) {
        roleConnection
      }
    }

  private def searchUsersInGroup(
      pagination: Pagination,
      searchText: String,
      groupId: String,
      recursive: Boolean
  )(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[UserDirectoryUserView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      UserDirectoryQueries.searchUsersInGroup(
        searchText,
        groupId,
        recursive,
        first,
        last,
        before,
        after
      ) {
        userConnection
      }
    }
}

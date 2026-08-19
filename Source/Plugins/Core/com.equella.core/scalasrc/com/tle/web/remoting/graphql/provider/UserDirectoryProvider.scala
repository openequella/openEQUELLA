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

package com.tle.web.remoting.graphql.provider

import caliban.relay.{Base64Cursor, Pagination}
import com.tle.common.security.SecurityConstants
import com.tle.common.usermanagement.user.valuebean.{GroupBean, UserBean, RoleBean}
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.services.user.UserService
import com.tle.web.remoting.graphql.ErrorCode
import com.tle.web.remoting.graphql.schema.{Page, paginationOffsetLimit}
import com.tle.web.remoting.graphql.schema.types.{
  Group,
  GroupConnection,
  GroupWithId,
  Role,
  RoleConnection,
  RoleWithId,
  User,
  UserConnection,
  UserWithId
}

import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

/** A provider for user directory operations backed by [[UserService]]. The user directory
  * aggregates results from all configured user management plugins (e.g. LDAP, Internal TLE, etc.).
  */
@Bind
@Singleton
class UserDirectoryProvider @Inject() (userService: UserService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[UserDirectoryProvider])

  /** Compute a [[Page]] by counting the total result set, deriving offset/limit from
    * [[Pagination]], and then fetching only the required slice via `fetch`.
    *
    * @param pagination
    *   the Relay-style pagination parameters
    * @param count
    *   by-name expression that returns the total number of matching users, groups, or roles
    * @param fetch
    *   function from (limit, offset) to the users, groups, or roles for this page
    * @tparam T
    *   the entity type — [[User]], [[Group]], or [[Role]]
    * @return
    *   a [[Page]] ready to be wrapped in a Connection type
    */
  private def paginatePage[T](
      pagination: Pagination[Base64Cursor],
      count: => Int,
      fetch: (Int, Int) => List[T]
  ): Page[T] = {
    val total           = count
    val (offset, limit) = paginationOffsetLimit(pagination, total)
    val items           = if (total > 0) fetch(limit, offset) else List.empty
    Page(items, total, offset, limit)
  }

  /** Builds a page for [[searchUsersInGroup]].
    */
  private def searchUsersInGroupPage(
      query: String,
      groupId: String,
      recursive: Boolean,
      pagination: Pagination[Base64Cursor]
  ): Page[User] =
    paginatePage(
      pagination,
      userService.countUsers(query, groupId, recursive),
      (limit, offset) =>
        javaUsersToScala(userService.searchUsers(query, groupId, recursive, limit, offset))
    )

  /** Builds a page for [[usersInGroup]].
    */
  private def usersInGroupPage(
      groupId: String,
      recursive: Boolean,
      pagination: Pagination[Base64Cursor]
  ): Page[User] =
    paginatePage(
      pagination,
      userService.countUsersInGroup(groupId, recursive),
      (limit, offset) =>
        javaUsersToScala(userService.getUsersInGroup(groupId, recursive, limit, offset))
    )

  /** Builds a page for [[searchGroupsInParent]].
    */
  private def searchGroupsInParentPage(
      query: String,
      parentGroupId: String,
      pagination: Pagination[Base64Cursor]
  ): Page[Group] =
    paginatePage(
      pagination,
      userService.countGroups(query, parentGroupId),
      (limit, offset) =>
        javaGroupsToScala(userService.searchGroups(query, parentGroupId, limit, offset))
    )

  // Formats the pagination parameters as a short string for debug logging.
  private def paginationInfo(pagination: Pagination[Base64Cursor]): String =
    s"[count=${pagination.count}, cursor=${pagination.cursor}]"

  // Helper method to convert a Java List of UserBeans to a Scala List of Users
  private def javaUsersToScala(beans: java.util.List[UserBean]): List[User] =
    beans.asScala.map(User(_)).toList

  // Helper method to convert a Java List of GroupBeans to a Scala List of Groups
  private def javaGroupsToScala(beans: java.util.List[GroupBean]): List[Group] =
    beans.asScala.map(Group(_)).toList

  // Helper method to convert a Java List of RoleBeans to a Scala List of Roles
  private def javaRolesToScala(beans: java.util.List[RoleBean]): List[Role] =
    beans.asScala.map(Role(_)).toList

  private def userNotFound(userId: String): ProviderError =
    ProviderError(s"User with id of $userId not found", ErrorCode.NOT_FOUND)

  private def requireUser(userId: String): Either[ProviderError, String] =
    Option(userService.getInformationForUser(userId))
      .map(_ => userId)
      .toRight(userNotFound(userId))

  private def groupNotFound(groupId: String): ProviderError =
    ProviderError(s"Group with id of $groupId not found", ErrorCode.NOT_FOUND)

  private def requireGroup(groupId: String): Either[ProviderError, String] =
    Option(userService.getInformationForGroup(groupId))
      .map(_ => groupId)
      .toRight(groupNotFound(groupId))

  private def roleNotFound(roleId: String): ProviderError =
    ProviderError(s"Role with id of $roleId not found", ErrorCode.NOT_FOUND)

  private def requireRole(roleId: String): Either[ProviderError, Role] =
    Option(userService.getInformationForRole(roleId)).map(Role(_)).toRight(roleNotFound(roleId))

  // ---------------------------------------------------------------------------
  // User operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single user by their unique ID.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   the [[User]], or a NOT_FOUND error if the user cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def userById(userId: String): Either[ProviderError, User] = {
    LOGGER.debug("Retrieving user by ID: {}", userId)
    Option(userService.getInformationForUser(userId))
      .map(User(_))
      .toRight(userNotFound(userId))
  }

  /** Retrieve information for multiple users by their unique IDs. IDs that could not be resolved
    * are absent from the result.
    *
    * @param userIds
    *   a set of user unique IDs to look up
    * @return
    *   a list of [[UserWithId]] for each resolved ID
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def usersByIds(userIds: Set[String]): List[UserWithId] = {
    LOGGER.debug("Retrieving users by IDs: {}", userIds)
    userService
      .getInformationForUsers(userIds.asJava)
      .asScala
      .map { case (id, bean) => UserWithId(id, User(bean)) }
      .toList
  }

  /** Search for users matching the given query with pagination. Wildcards at the start and end of
    * the query are implied.
    *
    * @param query
    *   the search query to match against username, first name, or last name
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[UserConnection]] containing the page of matching users
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchUsers(query: String, pagination: Pagination[Base64Cursor]): UserConnection = {
    LOGGER.debug(
      "Searching users with query: {}, pagination: {}",
      query,
      paginationInfo(pagination)
    )
    UserConnection(
      paginatePage(
        pagination,
        userService.countUsers(query),
        (limit, offset) => javaUsersToScala(userService.searchUsers(query, limit, offset))
      )
    )
  }

  /** Search for users within the specified group (and optionally its subgroups) with pagination.
    *
    * @param query
    *   the search query to match against username, first name, or last name
    * @param parentGroupId
    *   the group to restrict the search to
    * @param recursive
    *   whether to search subgroups recursively
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[UserConnection]] containing the page of matching users, or a NOT_FOUND error if the
    *   group cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchUsersInGroup(
      query: String,
      parentGroupId: String,
      recursive: Boolean,
      pagination: Pagination[Base64Cursor]
  ): Either[ProviderError, UserConnection] = {
    LOGGER.debug(
      "Searching users with query: {}, parentGroupId: {}, recursive: {}, pagination: {}",
      query,
      parentGroupId,
      recursive,
      paginationInfo(pagination)
    )
    requireGroup(parentGroupId).map { validParentGroupId =>
      UserConnection(searchUsersInGroupPage(query, validParentGroupId, recursive, pagination))
    }
  }

  /** Retrieve all roles assigned to the given user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of [[Role]] objects, or a NOT_FOUND error if the user cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def rolesForUser(userId: String): Either[ProviderError, List[Role]] = {
    LOGGER.debug("Retrieving roles for user: {}", userId)
    requireUser(userId).map { validUserId =>
      javaRolesToScala(userService.getRolesForUser(validUserId))
    }
  }

  /** Retrieve the IDs of all groups that contain the specified user. This is recursive: if a user
    * belongs to a child group, parent groups are also treated as containing the user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of group unique IDs, or a NOT_FOUND error if the user cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupIdsForUser(userId: String): Either[ProviderError, List[String]] = {
    LOGGER.debug("Retrieving group IDs for user: {}", userId)
    requireUser(userId).map { validUserId =>
      userService.getGroupIdsContainingUser(validUserId).asScala.toList
    }
  }

  /** Retrieve all groups (including subgroups) that contain the specified user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of [[Group]] objects, or a NOT_FOUND error if the user cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupsForUser(userId: String): Either[ProviderError, List[Group]] = {
    LOGGER.debug("Retrieving groups for user: {}", userId)
    requireUser(userId).map { validUserId =>
      javaGroupsToScala(userService.getGroupsContainingUser(validUserId))
    }
  }

  /** List all users in the specified group with pagination.
    *
    * @param groupId
    *   the unique ID of the group
    * @param recursive
    *   whether to include users from subgroups
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[UserConnection]] containing the page of users, or a NOT_FOUND error if the group cannot
    *   be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def usersInGroup(
      groupId: String,
      recursive: Boolean,
      pagination: Pagination[Base64Cursor]
  ): Either[ProviderError, UserConnection] = {
    LOGGER.debug(
      "Retrieving users in group: {}, recursive: {}, pagination: {}",
      groupId,
      recursive,
      paginationInfo(pagination)
    )
    requireGroup(groupId).map { validGroupId =>
      UserConnection(usersInGroupPage(validGroupId, recursive, pagination))
    }
  }

  // ---------------------------------------------------------------------------
  // Group operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single group by its unique ID.
    *
    * @param groupId
    *   the unique ID of the group
    * @return
    *   the [[Group]], or a NOT_FOUND error if the group cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupById(groupId: String): Either[ProviderError, Group] = {
    LOGGER.debug("Retrieving group by ID: {}", groupId)
    Option(userService.getInformationForGroup(groupId))
      .map(Group(_))
      .toRight(groupNotFound(groupId))
  }

  /** Retrieve information for multiple groups by their unique IDs. IDs that could not be resolved
    * are absent from the result.
    *
    * @param groupIds
    *   a set of group unique IDs to look up
    * @return
    *   a list of [[GroupWithId]] for each resolved ID
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupsByIds(groupIds: Set[String]): List[GroupWithId] = {
    LOGGER.debug("Retrieving groups by IDs: {}", groupIds)
    userService
      .getInformationForGroups(groupIds.asJava)
      .asScala
      .map { case (id, bean) => GroupWithId(id, Group(bean)) }
      .toList
  }

  /** Search for groups matching the given query across the entire group hierarchy with pagination.
    *
    * @param query
    *   the search query - wildcards at start and end are implied
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[GroupConnection]] containing the page of matching groups
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchGroups(query: String, pagination: Pagination[Base64Cursor]): GroupConnection = {
    LOGGER.debug(
      "Searching groups with query: {}, pagination: {}",
      query,
      paginationInfo(pagination)
    )
    GroupConnection(
      paginatePage(
        pagination,
        userService.countGroups(query),
        (limit, offset) => javaGroupsToScala(userService.searchGroups(query, limit, offset))
      )
    )
  }

  /** Search for groups matching the given query within the specified parent group with pagination.
    *
    * @param query
    *   the search query - wildcards at start and end are implied
    * @param parentGroupId
    *   the unique ID of the parent group to restrict the search to
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[GroupConnection]] containing the page of matching groups, or a NOT_FOUND error if the
    *   parent group cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchGroupsInParent(
      query: String,
      parentGroupId: String,
      pagination: Pagination[Base64Cursor]
  ): Either[ProviderError, GroupConnection] = {
    LOGGER.debug(
      "Searching groups with query: {}, parentGroupId: {}, pagination: {}",
      query,
      parentGroupId,
      paginationInfo(pagination)
    )
    requireGroup(parentGroupId).map { validParentGroupId =>
      GroupConnection(searchGroupsInParentPage(query, validParentGroupId, pagination))
    }
  }

  /** Retrieve the parent group of the specified group, if one exists.
    *
    * @param groupId
    *   the unique ID of the group
    * @return
    *   the parent [[Group]], `None` if the group has no parent, or a NOT_FOUND error if the group
    *   cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def parentGroup(groupId: String): Either[ProviderError, Option[Group]] = {
    LOGGER.debug("Retrieving parent group of: {}", groupId)
    requireGroup(groupId).map { validGroupId =>
      Option(userService.getParentGroupForGroup(validGroupId)).map(Group(_))
    }
  }

  // ---------------------------------------------------------------------------
  // Role operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single role by its unique ID.
    *
    * @param roleId
    *   the unique ID of the role
    * @return
    *   the [[Role]], or a NOT_FOUND error if the role cannot be resolved
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def roleById(roleId: String): Either[ProviderError, Role] = {
    LOGGER.debug("Retrieving role by ID: {}", roleId)
    requireRole(roleId)
  }

  /** Retrieve information for multiple roles by their unique IDs. IDs that could not be resolved
    * are absent from the result.
    *
    * @param roleIds
    *   a set of role unique IDs to look up
    * @return
    *   a list of [[RoleWithId]] for each resolved ID
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def rolesByIds(roleIds: Set[String]): List[RoleWithId] = {
    LOGGER.debug("Retrieving roles by IDs: {}", roleIds)
    userService
      .getInformationForRoles(roleIds.asJava)
      .asScala
      .map { case (id, bean) => RoleWithId(id, Role(bean)) }
      .toList
  }

  /** Search for roles matching the given query with pagination. Wildcards at start and end are
    * implied.
    *
    * @param query
    *   the search query
    * @param pagination
    *   the pagination parameters
    * @return
    *   a [[RoleConnection]] containing the page of matching roles
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchRoles(query: String, pagination: Pagination[Base64Cursor]): RoleConnection = {
    LOGGER.debug(
      "Searching roles with query: {}, pagination: {}",
      query,
      paginationInfo(pagination)
    )
    RoleConnection(
      paginatePage(
        pagination,
        userService.countRoles(query),
        (limit, offset) => javaRolesToScala(userService.searchRoles(query, limit, offset))
      )
    )
  }
}

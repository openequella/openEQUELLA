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

import com.tle.common.security.SecurityConstants
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.services.user.UserService
import com.tle.web.remoting.graphql.schema.types.{
  GroupWithId,
  Role,
  RoleWithId,
  User,
  Group,
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

  // ---------------------------------------------------------------------------
  // User operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single user by their unique ID.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   the [[User]], or `None` if not found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def userById(userId: String): Option[User] = {
    LOGGER.debug("Retrieving user by ID: {}", userId)
    Option(userService.getInformationForUser(userId)).map(User(_))
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

  /** Search for users matching the given query. Wildcards at the start and end of the query are
    * implied by the underlying service (e.g. `mit` will match `smith`).
    *
    * @param query
    *   the search query to match against username, first name, or last name
    * @return
    *   a list of matching [[User]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchUsers(query: String): List[User] = {
    LOGGER.debug("Searching users with query: {}", query)
    userService.searchUsers(query).asScala.map(User(_)).toList
  }

  /** Search for users within the specified group (and optionally its subgroups).
    *
    * @param query
    *   the search query to match against username, first name, or last name
    * @param parentGroupId
    *   the group to restrict the search to, or `None` for all groups
    * @param recursive
    *   whether to search subgroups recursively
    * @return
    *   a list of matching [[User]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchUsersInGroup(
      query: String,
      parentGroupId: Option[String],
      recursive: Boolean
  ): List[User] = {
    LOGGER.debug(
      "Searching users with query: {}, parentGroupId: {}, recursive: {}",
      query,
      parentGroupId,
      recursive
    )
    userService
      .searchUsers(query, parentGroupId.orNull, recursive)
      .asScala
      .map(User(_))
      .toList
  }

  /** Retrieve all roles assigned to the given user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of [[Role]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def rolesForUser(userId: String): List[Role] = {
    LOGGER.debug("Retrieving roles for user: {}", userId)
    userService.getRolesForUser(userId).asScala.map(Role(_)).toList
  }

  /** Retrieve the IDs of all groups that contain the specified user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of group unique IDs
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupIdsForUser(userId: String): List[String] = {
    LOGGER.debug("Retrieving group IDs for user: {}", userId)
    userService.getGroupIdsContainingUser(userId).asScala.toList
  }

  /** Retrieve all groups (including subgroups) that contain the specified user.
    *
    * @param userId
    *   the unique ID of the user
    * @return
    *   a list of [[Group]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupsForUser(userId: String): List[Group] = {
    LOGGER.debug("Retrieving groups for user: {}", userId)
    userService.getGroupsContainingUser(userId).asScala.map(Group(_)).toList
  }

  /** List all users in the specified group.
    *
    * @param groupId
    *   the unique ID of the group
    * @param recursive
    *   whether to include users from subgroups
    * @return
    *   a list of [[User]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def usersInGroup(groupId: String, recursive: Boolean): List[User] = {
    LOGGER.debug("Retrieving users in group: {}, recursive: {}", groupId, recursive)
    userService.getUsersInGroup(groupId, recursive).asScala.map(User(_)).toList
  }

  // ---------------------------------------------------------------------------
  // Group operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single group by its unique ID.
    *
    * @param groupId
    *   the unique ID of the group
    * @return
    *   the [[Group]], or `None` if not found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupById(groupId: String): Option[Group] = {
    LOGGER.debug("Retrieving group by ID: {}", groupId)
    Option(userService.getInformationForGroup(groupId)).map(Group(_))
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

  /** Search for groups matching the given query across the entire group hierarchy.
    *
    * @param query
    *   the search query - wildcards at start and end are implied
    * @return
    *   a list of matching [[Group]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchGroups(query: String): List[Group] = {
    LOGGER.debug("Searching groups with query: {}", query)
    userService.searchGroups(query).asScala.map(Group(_)).toList
  }

  /** Search for groups matching the given query within the specified parent group.
    *
    * @param query
    *   the search query - wildcards at start and end are implied
    * @param parentGroupId
    *   the unique ID of the parent group to restrict the search to
    * @return
    *   a list of matching [[Group]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchGroupsInParent(query: String, parentGroupId: String): List[Group] = {
    LOGGER.debug("Searching groups with query: {}, parentGroupId: {}", query, parentGroupId)
    userService.searchGroups(query, parentGroupId).asScala.map(Group(_)).toList
  }

  /** Retrieve the parent group of the specified group, if one exists.
    *
    * @param groupId
    *   the unique ID of the group
    * @return
    *   the parent [[Group]], or `None` if the group has no parent
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def parentGroup(groupId: String): Option[Group] = {
    LOGGER.debug("Retrieving parent group of: {}", groupId)
    Option(userService.getParentGroupForGroup(groupId)).map(Group(_))
  }

  // ---------------------------------------------------------------------------
  // Role operations
  // ---------------------------------------------------------------------------

  /** Retrieve information for a single role by its unique ID.
    *
    * @param roleId
    *   the unique ID of the role
    * @return
    *   the [[Role]], or `None` if not found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def roleById(roleId: String): Option[Role] = {
    LOGGER.debug("Retrieving role by ID: {}", roleId)
    Option(userService.getInformationForRole(roleId)).map(Role(_))
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

  /** Search for roles matching the given query.
    *
    * @param query
    *   the search query - wildcards at start and end are implied
    * @return
    *   a list of matching [[Role]] objects
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchRoles(query: String): List[Role] = {
    LOGGER.debug("Searching roles with query: {}", query)
    userService.searchRoles(query).asScala.map(Role(_)).toList
  }
}

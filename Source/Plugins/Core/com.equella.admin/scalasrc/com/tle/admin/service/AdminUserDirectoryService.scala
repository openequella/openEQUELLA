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

package com.tle.admin.service

import com.tle.common.usermanagement.user.valuebean.{GroupBean, RoleBean, UserBean}

import java.util
import java.util.Optional

/** Admin client service for user directory lookups and searches.
  */
trait AdminUserDirectoryService {
  // User operations

  /** Get information for the specified user.
    *
    * @param userId
    *   The unique ID of the user to get
    * @return
    *   The user if it exists, or empty if it does not or the API reports it as not found
    */
  def getInformationForUser(userId: String): Optional[UserBean]

  /** Resolves user IDs to user details, omitting IDs that cannot be resolved.
    *
    * @param userUniqueIDs
    *   The unique IDs of the users to get
    * @return
    *   A map of user unique ID to user details
    */
  def getInformationForUsers(userUniqueIDs: util.Collection[String]): util.Map[String, UserBean]

  /** Lists roles assigned to a user.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The roles assigned to the user
    */
  def getRolesForUser(userId: String): util.List[RoleBean]

  /** Lists IDs of groups containing a user, including parent groups for recursive membership.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The IDs of groups containing the user
    */
  def getGroupIdsContainingUser(userId: String): util.List[String]

  /** Get a list of groups that the user identified by userId belongs to. Will do a recursive search
    * of groups.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The groups containing the user
    */
  def getGroupsContainingUser(userId: String): util.List[GroupBean]

  /** The user fields the query is matched with is User Management plugin dependent, but generally
    * will attempt to match with username, first name, last name. Wildcards at the start and end of
    * the query are implied. E.g. 'mit' will match 'smith'
    *
    * @param query
    *   The username, first name or last name to search for
    * @return
    *   The list of users that match the query, or an empty list if none are found
    */
  def searchUsers(query: String): util.List[UserBean]

  /** Same as `searchUsers(query)`, but filters the results to users contained in the specified
    * group, or subgroups if recursive.
    *
    * @param query
    *   The username, first name or last name to search for
    * @param parentGroupId
    *   The highest-level group to search
    * @param recurse
    *   Search subgroups
    * @return
    *   The list of users that match the query, or an empty list if none are found
    */
  def searchUsersInGroup(
      query: String,
      parentGroupId: String,
      recurse: Boolean
  ): util.List[UserBean]

  // Group operations

  /** Resolves a single group ID.
    *
    * @param groupId
    *   The unique ID of the group to get
    * @return
    *   The group if it exists, or empty if it does not or the API reports it as not found
    */
  def getInformationForGroup(groupId: String): Optional[GroupBean]

  /** Resolves group IDs to group details, omitting IDs that cannot be resolved.
    *
    * @param groupIds
    *   The unique IDs of the groups to get
    * @return
    *   A map of group unique ID to group details
    */
  def getInformationForGroups(groupIds: util.Collection[String]): util.Map[String, GroupBean]

  /** Resolves the parent group of a group.
    *
    * @param groupId
    *   The unique ID of the group
    * @return
    *   The parent group if it exists, or empty if the group has no parent or the API reports the
    *   group as not found
    */
  def getParentGroupForGroup(groupId: String): Optional[GroupBean]

  /** Lists users in a group, optionally including users from subgroups.
    *
    * @param groupId
    *   The unique ID of the group
    * @param recursive
    *   Whether to include users from subgroups
    * @return
    *   The users in the group
    */
  def getUsersInGroup(groupId: String, recursive: Boolean): util.List[UserBean]

  /** Searches groups by free-text query.
    *
    * @param query
    *   The query to search for matching groups with
    * @return
    *   The list of groups that match the query, or an empty list if none are found
    */
  def searchGroups(query: String): util.List[GroupBean]

  /** Searches groups scoped to a parent group.
    *
    * @param query
    *   The query to search for matching groups with
    * @param parentGroupId
    *   The unique ID of the parent group
    * @return
    *   The list of groups that match the query, or an empty list if none are found
    */
  def searchGroupsInParent(query: String, parentGroupId: String): util.List[GroupBean]

  // Role operations

  /** Resolves a single role ID.
    *
    * @param roleId
    *   The unique ID of the role to get
    * @return
    *   The role if it exists, or empty if it does not or the API reports it as not found
    */
  def getInformationForRole(roleId: String): Optional[RoleBean]

  /** Resolves role IDs to role details, omitting IDs that cannot be resolved.
    *
    * @param roleIds
    *   The unique IDs of the roles to get
    * @return
    *   A map of role unique ID to role details
    */
  def getInformationForRoles(roleIds: util.Collection[String]): util.Map[String, RoleBean]

  /** Searches roles by free-text query.
    *
    * @param query
    *   The query to search for matching roles with
    * @return
    *   The list of roles that match the query, or an empty list if none are found
    */
  def searchRoles(query: String): util.List[RoleBean]
}

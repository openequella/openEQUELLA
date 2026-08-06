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

package com.tle.plugins.ump

import com.tle.common.usermanagement.user.valuebean.{GroupBean, RoleBean, UserBean}

import java.util

/** Lookup and search contracts for users, groups, and roles, including paginated variants of the
  * search operations.
  */
trait UserDirectoryQueries {

  // ---------------------------------------------------------------------------
  // Users
  // ---------------------------------------------------------------------------

  /** Resolve the given userIds into UserBean objects, keyed by unique ID. IDs that cannot be
    * resolved are omitted from the returned map.
    *
    * @param userIds
    *   A collection of user unique IDs
    * @return
    *   A map of unique ID to UserBean, containing only the resolved users
    */
  def getInformationForUsers(userIds: util.Collection[String]): util.Map[String, UserBean]

  /** Get the UserBean for the given user ID.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The matching UserBean, or null if no user is found
    */
  def getInformationForUser(userId: String): UserBean

  /** Get the roles assigned to the user identified by userId.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The roles held by the user
    */
  def getRolesForUser(userId: String): util.List[RoleBean]

  /** Get a list of groups that the user identified by userId belongs to. Will do a recursive search
    * of groups.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The groups the user belongs to
    */
  def getGroupsContainingUser(userId: String): util.List[GroupBean]

  /** Same as [[getGroupsContainingUser]], but returns only the unique IDs of the groups.
    *
    * @param userId
    *   The unique ID of the user
    * @return
    *   The unique IDs of the groups the user belongs to
    */
  def getGroupIdsContainingUser(userId: String): util.List[String]

  /** Retrieves the users belonging to the specified group. If `recursive` is true, users in nested
    * subgroups are also included.
    *
    * @param groupId
    *   The unique ID of the group to retrieve users for
    * @param recursive
    *   Whether to include users from subgroups recursively
    * @return
    *   The users in the group
    */
  def getUsersInGroup(groupId: String, recursive: Boolean): util.List[UserBean]

  /** Returns the total count of users matching the given query (with implied wildcards). Used by
    * paginated search to build pagination metadata.
    *
    * @param query
    *   The username, first name or last name to search for
    * @return
    *   Total number of matching users
    */
  def countUsers(query: String): Int

  /** Returns the total number of users contained in the specified group (and optionally its
    * subgroups) whose username, first name, or last name matches the given query. Wildcards are
    * implied at both ends of the query string.
    *
    * @param query
    *   The username, first name or last name to search for
    * @param parentGroupId
    *   The highest level group to search
    * @param recursive
    *   Whether to search subgroups recursively
    * @return
    *   Total number of matching users within the group
    */
  def countUsers(query: String, parentGroupId: String, recursive: Boolean): Int

  /** Search for users whose username, first name, or last name matches the given query. Wildcards
    * at the start and end of the query are implied. E.g. 'mit' will match 'smith'
    *
    * @param query
    *   The username, first name or last name to search for
    * @return
    *   The matching users
    */
  def searchUsers(query: String): util.List[UserBean]

  /** Search for users whose username, first name, or last name matches the given query, returning
    * only the slice defined by `limit` and `offset`. Wildcards are implied at both ends of the
    * query string. Matches are aggregated across all configured user management plugins.
    *
    * @param query
    *   The username, first name or last name to search for
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The matching users for the requested page; empty list if offset >= total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def searchUsers(query: String, limit: Int, offset: Int): util.List[UserBean]

  /** Same as [[searchUsers(String)]], but filters the results to users contained in the specified
    * group, or subgroups if recursive.
    *
    * @param query
    *   The username, first name or last name to search for
    * @param parentGroupId
    *   The highest-level group to search, or null if all groups to be searched. Avoid passing
    *   `null`; although the current implementation falls back to `searchUsers(String query)` when
    *   this value is `null`, callers should prefer using that method directly.
    * @param recurse
    *   Search subgroups
    * @return
    *   The matching users
    */
  def searchUsers(query: String, parentGroupId: String, recurse: Boolean): util.List[UserBean]

  /** Search for users contained in the specified group (and optionally its subgroups), returning
    * only the slice defined by `limit` and `offset`. Wildcards are implied at both ends. Matches
    * against username, first name, and last name.
    *
    * @param query
    *   The username, first name or last name to search for
    * @param parentGroupId
    *   The highest level group to search
    * @param recursive
    *   Whether to search subgroups recursively
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The matching users for the requested page; empty list if offset >= total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def searchUsers(
      query: String,
      parentGroupId: String,
      recursive: Boolean,
      limit: Int,
      offset: Int
  ): util.List[UserBean]

  /** Returns the total number of users that belong to the specified group. If `recursive` is true,
    * users in nested subgroups are also counted.
    *
    * @param groupId
    *   The unique ID of the group to count users for
    * @param recursive
    *   Whether to count users in subgroups recursively
    * @return
    *   Total number of users in the group
    */
  def countUsersInGroup(groupId: String, recursive: Boolean): Int

  /** Retrieves a slice of users belonging to the specified group. If `recursive` is true, users in
    * nested subgroups are also included. Results are ordered consistently with the non-paginated
    * variant.
    *
    * @param groupId
    *   The unique ID of the group to retrieve users for
    * @param recursive
    *   Whether to include users from subgroups recursively
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The users in the group for the requested page; empty list if offset >= total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def getUsersInGroup(
      groupId: String,
      recursive: Boolean,
      limit: Int,
      offset: Int
  ): util.List[UserBean]

  // ---------------------------------------------------------------------------
  // Groups
  // ---------------------------------------------------------------------------

  /** Get the GroupBean for the given group ID.
    *
    * @param uuid
    *   The unique ID of the group
    * @return
    *   The matching GroupBean, or null if no group is found
    */
  def getInformationForGroup(uuid: String): GroupBean

  /** Resolve the given groupIDs into GroupBean objects, keyed by unique ID. IDs that cannot be
    * resolved are omitted from the returned map.
    *
    * @param groupIds
    *   A collection of group unique IDs
    * @return
    *   A map of unique ID to GroupBean, containing only the resolved groups
    */
  def getInformationForGroups(groupIds: util.Collection[String]): util.Map[String, GroupBean]

  /** Get the parent group of the group identified by groupId.
    *
    * @param groupId
    *   The unique ID of the group to find the parent of
    * @return
    *   The parent group, or null if the group has no parent
    */
  def getParentGroupForGroup(groupId: String): GroupBean

  /** Returns the total number of groups whose name matches the given query. Wildcards are implied
    * at both ends of the query string.
    *
    * @param query
    *   The group name fragment to search for
    * @return
    *   Total number of matching groups
    */
  def countGroups(query: String): Int

  /** Returns the total number of groups whose name matches the given query within the specified
    * parent group. Wildcards are implied at both ends of the query string.
    *
    * @param query
    *   The group name fragment to search for
    * @param parentGroupId
    *   The unique ID of the parent group to restrict the search to
    * @return
    *   Total number of matching groups within the parent group
    */
  def countGroups(query: String, parentGroupId: String): Int

  /** Search for groups whose name matches the given query. Wildcards at the start and end of the
    * query are implied.
    *
    * @param query
    *   The group name fragment to search for
    * @return
    *   The matching groups
    */
  def searchGroups(query: String): util.List[GroupBean]

  /** Search for groups whose name matches the given query anywhere in the group hierarchy,
    * returning only the slice defined by `limit` and `offset`. Wildcards are implied at both ends
    * of the query string.
    *
    * @param query
    *   The group name fragment to search for
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The matching groups for the requested page; empty list if offset >= total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def searchGroups(query: String, limit: Int, offset: Int): util.List[GroupBean]

  /** Same as [[searchGroups(String)]], but filters the results to groups contained within the
    * specified parent group.
    *
    * @param query
    *   The group name fragment to search for
    * @param parentGroupId
    *   The highest-level group to search
    * @return
    *   The matching groups
    */
  def searchGroups(query: String, parentGroupId: String): util.List[GroupBean]

  /** Search for groups whose name matches the given query within the specified parent group,
    * returning only the slice defined by `limit` and `offset`. Wildcards are implied at both ends
    * of the query string.
    *
    * @param query
    *   The group name fragment to search for
    * @param parentGroupId
    *   The unique ID of the parent group to restrict the search to
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The matching groups within the parent group for the requested page; empty list if offset >=
    *   total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def searchGroups(
      query: String,
      parentGroupId: String,
      limit: Int,
      offset: Int
  ): util.List[GroupBean]

  // ---------------------------------------------------------------------------
  // Roles
  // ---------------------------------------------------------------------------

  /** Get the RoleBean for the given role ID.
    *
    * @param uuid
    *   The unique ID of the role
    * @return
    *   The matching RoleBean, or null if no role is found
    */
  def getInformationForRole(uuid: String): RoleBean

  /** Resolve the given roleIDs into RoleBean objects, keyed by unique ID. IDs that cannot be
    * resolved are omitted from the returned map.
    *
    * @param roleIDs
    *   A collection of role unique IDs
    * @return
    *   A map of unique ID to RoleBean, containing only the resolved roles
    */
  def getInformationForRoles(roleIDs: util.Collection[String]): util.Map[String, RoleBean]

  /** Returns the total number of roles whose name matches the given query. Wildcards are implied at
    * both ends of the query string.
    *
    * @param query
    *   The role name fragment to search for
    * @return
    *   Total number of matching roles
    */
  def countRoles(query: String): Int

  /** Search for roles whose name matches the given query. Wildcards at the start and end of the
    * query are implied.
    *
    * @param query
    *   The role name fragment to search for
    * @return
    *   The matching roles
    */
  def searchRoles(query: String): util.List[RoleBean]

  /** Search for roles whose name matches the given query, returning only the slice defined by
    * `limit` and `offset`. Wildcards are implied at both ends of the query string.
    *
    * @param query
    *   The role name fragment to search for
    * @param limit
    *   Maximum number of results to return (must be >= 0)
    * @param offset
    *   Zero-based start index of the first result to return (must be >= 0)
    * @return
    *   The matching roles for the requested page; empty list if offset >= total count
    * @throws IllegalArgumentException
    *   if limit or offset is negative
    */
  def searchRoles(query: String, limit: Int, offset: Int): util.List[RoleBean]
}

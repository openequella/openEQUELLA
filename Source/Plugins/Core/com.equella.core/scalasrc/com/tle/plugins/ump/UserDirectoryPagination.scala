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

/** Defines pagination contracts for user, group, and role search operations. */
trait UserDirectoryPagination {

  // ---------------------------------------------------------------------------
  // Users
  // ---------------------------------------------------------------------------

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

  /** Returns the total number of roles whose name matches the given query. Wildcards are implied at
    * both ends of the query string.
    *
    * @param query
    *   The role name fragment to search for
    * @return
    *   Total number of matching roles
    */
  def countRoles(query: String): Int

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

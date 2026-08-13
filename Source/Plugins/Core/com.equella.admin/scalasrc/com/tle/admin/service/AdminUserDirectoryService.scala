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
import com.tle.common.usermanagement.util.UserDirectoryEntityResolver

import java.util
import java.util.Optional

/** Admin client service for user directory lookups and searches.
  */
trait AdminUserDirectoryService extends UserDirectoryEntityResolver {
  // User operations

  /** Resolves user IDs to user details, omitting IDs that cannot be resolved.
    *
    * @param userUniqueIDs
    *   The unique IDs of the users to get
    * @return
    *   A map of user unique ID to user details
    */
  def getInformationForUsers(userUniqueIDs: util.Collection[String]): util.Map[String, UserBean]

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

  /** Resolves the parent group of a group.
    *
    * @param groupId
    *   The unique ID of the group
    * @return
    *   The parent group if it exists, or empty if the group has no parent or the API reports the
    *   group as not found
    */
  def getParentGroupForGroup(groupId: String): Optional[GroupBean]

  /** Searches groups by free-text query.
    *
    * @param query
    *   The query to search for matching groups with
    * @return
    *   The list of groups that match the query, or an empty list if none are found
    */
  def searchGroups(query: String): util.List[GroupBean]

  // Role operations

  /** Searches roles by free-text query.
    *
    * @param query
    *   The query to search for matching roles with
    * @return
    *   The list of roles that match the query, or an empty list if none are found
    */
  def searchRoles(query: String): util.List[RoleBean]
}

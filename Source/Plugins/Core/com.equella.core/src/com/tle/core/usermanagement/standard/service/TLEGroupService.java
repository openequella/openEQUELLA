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

package com.tle.core.usermanagement.standard.service;

import com.tle.beans.user.TLEGroup;
import com.tle.common.beans.exception.NotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TLEGroupService {
  String add(TLEGroup group);

  String add(String parentID, String name);

  TLEGroup createGroup(String groupID, String name);

  TLEGroup get(String id);

  TLEGroup getByName(String name);

  /**
   * Return the list of user IDs in the specified group. These can then be further resolved via the
   * UserService, as they may be TLEUsers, or they could be external users.
   *
   * @param parentGroupID The group to get users for.
   * @param recurse Whether to include users in subgroups.
   * @return The list of user IDs in the group (and subgroups if requested). Returns an empty list
   *     if no users are found.
   * @throws NotFoundException if the parent group cannot be found.
   */
  List<String> getUsersInGroup(String parentGroupID, boolean recurse);

  /**
   * Return the list of user IDs in the specified group. These can then be further resolved via the
   * UserService, as they may be TLEUsers, or they could be external users.
   *
   * @param parentGroupID The group to get users for.
   * @param recurse Whether to include users in subgroups.
   * @param limit The maximum number of users to return.
   * @param offset The number of users to skip before returning results.
   * @return The list of user IDs in the group (and subgroups if requested). Returns an empty list
   *     if no users are found.
   * @throws NotFoundException if the parent group cannot be found.
   */
  List<String> getUsersInGroup(
      String parentGroupID, boolean recurse, Integer limit, Integer offset);

  /**
   * Get all immediate child groups of the specified group, or all top-level groups if the group is
   * null.
   *
   * @param group The parent group, or null to get top-level groups.
   * @return The list of child groups.
   */
  List<TLEGroup> getGroupsInGroup(TLEGroup group);

  /**
   * Get all immediate child groups of the specified group, or all top-level groups if the group is
   * null.
   *
   * @param group The parent group, or null to get top-level groups.
   * @param limit The maximum number of groups to return.
   * @param offset The number of groups to skip before returning results.
   * @return The list of child groups.
   */
  List<TLEGroup> getGroupsInGroup(TLEGroup group, Integer limit, Integer offset);

  List<TLEGroup> getGroupsContainingUser(String userID, boolean recursive);

  List<TLEGroup> getInformationForGroups(Collection<String> groups);

  /**
   * Get information for a list of groups.
   *
   * @param groupIds The IDs of the groups to get information for
   * @param limit The maximum number of results to return
   * @param offset The number of results to skip before returning results
   * @return The list of groups that match the query - or an empty list if none are found
   */
  List<TLEGroup> getInformationForGroups(
      Collection<String> groupIds, Integer limit, Integer offset);

  String edit(final TLEGroup group);

  /**
   * Delete a group and optionally all its children. If the children are to be kept, they will be
   * moved to the parent of the group being deleted.
   *
   * @param groupID The ID of the group to delete
   * @param deleteChildren Whether to delete all children of the group (true) or move them to the
   *     parent (false)
   */
  void delete(String groupID, boolean deleteChildren);

  void addUserToGroup(String groupUuid, String userUuid);

  void removeUserFromGroup(String groupUuid, String userUuid);

  void removeAllUsersFromGroup(String groupUuid);

  /**
   * Searches for groups (anywhere within the group hierarchy) that match the query. No wildcards
   * are appended, so should be added as needed. (Asterisks are replaced with % in the query.)
   *
   * @param query The query to search for matching groups with
   * @return The list of groups that match the query - or an empty list if none are found
   */
  List<TLEGroup> search(String query);

  /**
   * Searches for groups (anywhere within the group hierarchy) that match the query. No wildcards
   * are appended, so should be added as needed. (Asterisks are replaced with % in the query.)
   *
   * @param query The query to search for matching groups with
   * @param limit The maximum number of results to return
   * @param offset The number of results to skip before returning results
   * @return The list of groups that match the query - or an empty list if none are found
   */
  List<TLEGroup> search(String query, Integer limit, Integer offset);

  List<TLEGroup> search(String query, String parentId);

  List<TLEGroup> search(String query, String userId, boolean allParents);

  String prepareQuery(String searchString);

  /**
   * Returns the number of users in the target group. (Non-recursive.)
   *
   * @param groupId The group ID.
   * @return The number of users in the group.
   */
  long countUsersInGroup(String groupId);

  /**
   * Returns the number of groups in the target group. (Non-recursive.)
   *
   * @param groupId The group ID.
   * @return The number of groups in the group.
   */
  long countGroupsInGroupById(String groupId);

  /**
   * Returns the number of groups in the target group. (Non-recursive.)
   *
   * @param parent The group, or null to count top-level groups.
   * @return The number of groups in the group.
   */
  long countGroupsInGroup(TLEGroup parent);

  /**
   * Returns the number of groups which have a name that matches the query. Useful alongside the
   * search(query) method to get the names of groups that match the query.
   *
   * @param query The query to search for matching groups with - accepts wildcards.
   * @return The number of groups that match the query
   */
  long countGroupsForQuery(String query);

  /**
   * Returns the number of groups which could be found.
   *
   * @param groupIds The group IDs to check.
   * @return The number of groups that could be found. If all could be found, then the returned
   *     number will be equal to the size of the groupIds set.
   */
  long countValidGroups(Set<String> groupIds);
}

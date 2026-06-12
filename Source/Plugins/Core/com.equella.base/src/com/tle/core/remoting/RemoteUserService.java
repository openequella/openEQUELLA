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

package com.tle.core.remoting;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This interface provides methods to retrieve information about users, groups and roles from the
 * user management system. The user management system is a plugin system, so the implementation of
 * this interface will be provided by a plugin. Typically, that is a collection of UserDirectory
 * plugins which are interacted with sequentially and the results aggregated.
 *
 * <p>This is different from the TLEUserService which is specifically focused on the internal TLE
 * Users. (And sits alongside TLEGroupService and TLERoleService.)
 */
public interface RemoteUserService {
  /**
   * Resolve the list of userUniqueIDs into a list of UserBean objects. The returned list of users
   * is in sync with the list of userUniqueIDs. If a given user ID cannot be resolved a null is
   * placed in the returned list.
   *
   * @param userUniqueIDs A collection of user unique IDs
   * @return A list of UserBean objects
   */
  Map<String, UserBean> getInformationForUsers(Collection<String> userUniqueIDs);

  /**
   * @return null if no user is found
   */
  UserBean getInformationForUser(String userid);

  List<RoleBean> getRolesForUser(String userid);

  List<String> getGroupIdsContainingUser(String userid);

  /**
   * Get a list of groups that the user identified by userid belongs to. Will do a recursive search
   * of groups.
   */
  List<GroupBean> getGroupsContainingUser(String userid);

  List<UserBean> getUsersInGroup(String groupId, boolean recursive);

  /**
   * The user fields the query is matched with is User Management plugin dependent, but generally
   * will attempt to match with username, first name, last name. Wildcards at the start and end of
   * the query are implied. E.g. 'mit' will match 'smith'
   *
   * @param query The username, first name or last name to search for
   */
  List<UserBean> searchUsers(String query);

  /**
   * Same as <code>searchUsers(query)</code>, but filters to the results to only users contained in
   * the specified group, or subgroups if recursive.
   *
   * @param query The username, first name or last name to search for
   * @param parentGroupID The highest-level group to search, or null if all groups to be searched.
   *     Avoid passing {@code null}; although the current implementation falls back to {@code
   *     searchUsers(String query)} when this value is {@code null}, callers should prefer using
   *     that method directly.
   * @param recurse Search subgroups
   */
  List<UserBean> searchUsers(String query, String parentGroupID, boolean recurse);

  GroupBean getInformationForGroup(String uuid);

  Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs);

  List<GroupBean> searchGroups(String query);

  List<GroupBean> searchGroups(String query, String parentGroupID);

  RoleBean getInformationForRole(String uuid);

  Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs);

  List<RoleBean> searchRoles(String query);

  GroupBean getParentGroupForGroup(String groupID);

  void keepAlive();

  List<String> getTokenSecretIds();

  UserManagementSettings getPluginConfig(String settingsConfig);

  UserManagementSettings getReadOnlyPluginConfig(String settingsConfig);

  void setPluginConfig(UserManagementSettings config);
}

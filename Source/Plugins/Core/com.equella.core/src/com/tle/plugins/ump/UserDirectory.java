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

package com.tle.plugins.ump;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Generic interface for any user management system. These include:
 *
 * <ul>
 *   <li>TLE internal
 *   <li>LDAP
 *   <li>CAS
 *   <li>LoginCentral/QERM
 *   <li>and others...
 * </ul>
 *
 * <p>In cases where a free-text query is a parameter, * indicates a wildcard by convention.
 *
 * <p>Parameters tagged with <i>(optional behaviour)</i> permit the User Management Plugin to ignore
 * their requirement.
 */
public interface UserDirectory {
  // TODO: move to file ChainedResult.java
  enum ChainResult {
    CONTINUE,
    STOP
  }

  /**
   * @return true if the settings should be re-saved to the DB after initialisation.
   */
  boolean initialise(UserDirectoryChain head, UserManagementSettings settings);

  /**
   * Authenticate the given username/password combination.
   *
   * @return a token for use on subsequent requests.
   */
  ModifiableUserState authenticateUser(String username, String password);

  /**
   * Authenticates a user from a username.
   *
   * @return null if token is invalid, else a valid user state for the given token.
   */
  ModifiableUserState authenticateUserFromUsername(String username, String privateData);

  /**
   * Authenticates a user from a token.
   *
   * @return null if token is invalid, else a valid user state.
   */
  ModifiableUserState authenticateToken(String token);

  /** Authenticates a user from a request. */
  ModifiableUserState authenticateRequest(HttpServletRequest request);

  /**
   * Called after successful user authentication to setup various extra UserState variables, such as
   * Roles.
   */
  void initUserState(ModifiableUserState state);

  /**
   * Called after creation of guest state to setup various extra variables, such as Roles. This
   * should be very light-weight, and never do network calls (for example) that may fail or timeout.
   */
  void initGuestUserState(ModifiableUserState state);

  void initSystemUserState(ModifiableUserState state);

  enum VerifyTokenResult {
    VALID,
    INVALID,
    PASS
  }

  /**
   * Ensure that the token passed is still valid for the current session. This method should be
   * light-weight.
   *
   * @return VALID or INVALID if token is handled by you, else PASS.
   */
  VerifyTokenResult verifyUserStateForToken(UserState userState, String token);

  /** Keeps the 'session' alive for the given token. */
  void keepAlive();

  /** Logout the 'session' for the given token. */
  void logout(UserState state);

  /**
   * Retrieve basic information regarding a user Id.
   *
   * @param userId the user Id to query
   * @return a UserBean object corresponding to the userId parameter
   */
  UserBean getInformationForUser(String userId);

  /**
   * Retrieve basic information regarding a list of user Ids.
   *
   * @param userIds zero or more users to query
   * @return a collection of UserBean objects corresponding to the userIds parameter
   */
  Map<String, UserBean> getInformationForUsers(Collection<String> userIds);

  /**
   * Retrieve basic information regarding a group Id.
   *
   * @param groupId a group ID to query.
   * @return a GroupBean object corresponding to the groupId parameter
   */
  GroupBean getInformationForGroup(String groupId);

  /**
   * Retrieve basic information regarding a list of group Ids.
   *
   * @param groupIds zero or more groups to query
   * @return a collection of GroupBean objects corresponding to the groupIds parameter
   */
  Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds);

  /** Retrieve basic information regarding a role Id. */
  RoleBean getInformationForRole(String roleId);

  /**
   * Retrieve basic information regarding a list of role Ids.
   *
   * @param roleIds zero or more roles to query
   * @return a collection of RoleBean objects corresponding to the roleIds parameter
   */
  Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds);

  /**
   * Retrieve the roles for the specified user.
   *
   * @param userId the user to query
   */
  Pair<ChainResult, Collection<RoleBean>> getRolesForUser(String userId);

  /**
   * Return a list of all group Ids for which the given user is considered a member. For example, if
   * user A is a member of group B, which in turn is a member of group C, both group B and group C
   * must be returned.
   *
   * @param userId the user to query
   * @return a collection of GroupBean objects for which the user is considered a member of
   */
  Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(String userId);

  /**
   * Return a list of all members of a given group. If <code>recursive</code> is false, then only
   * direct children (in a hierarchical sense) of the given group are to be returned.
   *
   * @param groupId The unique ID of the group to count users for
   * @param recursive Whether to count users in subgroups recursively
   * @return Total number of users in the group
   */
  int countUsersInGroup(String groupId, boolean recursive);

  /**
   * Return a list of all members of a given group. // TODO: RENAME TO getUsersInGroup
   *
   * @param groupId the group for which all user results must be a member.
   * @param recursive when false, only members directly in the given group should be returned.
   *     <i>(optional behaviour)</i> when true, all members of any sub-groups are also to be merged
   *     into the result set.
   * @return a pair containing the chain result and a collection of UserBean objects. If the group
   *     cannot be found, or no matching users can be resolved, the UserBean collection will be
   *     empty.
   */
  Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive);

  /**
   * Retrieves a slice of users belonging to the specified group.
   *
   * @param groupId The unique ID of the group to retrieve users for
   * @param recursive Whether to include users from subgroups recursively
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the users for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<UserBean> getUsersInGroup(String groupId, boolean recursive, int limit, int offset);

  /**
   * Returns the total count of users matching the given query.
   *
   * @param query The username, first name or last name to search for
   * @return Total number of matching users
   */
  int countUsers(String query);

  /**
   * Returns the total number of users in the specified group matching the given query.
   *
   * @param query The username, first name or last name to search for
   * @param parentGroupId The highest level group to search
   * @param recursive Whether to search subgroups recursively
   * @return Total number of matching users within the group
   */
  int countUsers(String query, String parentGroupId, boolean recursive);

  /**
   * Search for users, returning only the slice defined by {@code limit} and {@code offset}.
   *
   * @param query The username, first name or last name to search for
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the matching users for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<UserBean> searchUsers(String query, int limit, int offset);

  /**
   * Perform a free-text search over the user database on any fields deemed relevant by the external
   * system.
   *
   * @param query a string representing the free-text query.
   * @return a list of UserBean objects matching the query
   */
  Pair<ChainResult, Collection<UserBean>> searchUsers(String query);

  /**
   * Same as <code>searchUsers(query)</code> with additional filtering by group. If the
   * parentGroupId is not an empty string, then the resulting users should be a member of the given
   * group. If <code>recurse</code> is false, then only direct children (in a hierarchical sense) of
   * the given group are to be returned. <code>recurse</code> has no effect if the parentGroupId is
   * not specified.
   *
   * @param query a string representing the free-text query.
   * @param parentGroupId the group for which all user results must be a member. If it is an empty
   *     string, then users in any group may be considered.
   * @param recursive when false, only members directly in the given group should be returned.
   *     <i>(optional behaviour)</i> when true, all members of any sub-groups are also to be merged
   *     into the result set.
   * @return a list of UserBean objects matching the query
   */
  Pair<ChainResult, Collection<UserBean>> searchUsers(
      String query, String parentGroupId, boolean recursive);

  /**
   * Search for users in the specified group, returning only the slice defined by {@code limit} and
   * {@code offset}.
   *
   * @param query The username, first name or last name to search for
   * @param parentGroupId The highest level group to search
   * @param recursive Whether to search subgroups recursively
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the matching users for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<UserBean> searchUsers(
      String query, String parentGroupId, boolean recursive, int limit, int offset);

  /**
   * Returns the total number of groups matching the given query.
   *
   * @param query The group name fragment to search for
   * @return Total number of matching groups
   */
  int countGroups(String query);

  /**
   * Returns the total number of groups matching the given query within the specified parent group.
   *
   * @param query The group name fragment to search for
   * @param parentGroupId The unique ID of the parent group to restrict the search to
   * @return Total number of matching groups within the parent group
   */
  int countGroups(String query, String parentGroupId);

  /**
   * Perform a free-text search over the group database.
   *
   * @param query a string representing the free-text query.
   * @return a collection of GroupBean objects matching the query
   */
  Collection<GroupBean> searchGroups(String query);

  /**
   * Search for groups, returning only the slice defined by {@code limit} and {@code offset}.
   *
   * @param query The group name fragment to search for
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the matching groups for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<GroupBean> searchGroups(String query, int limit, int offset);

  /**
   * Same as {@link #searchGroups(String)} with additional filtering by parent group.
   *
   * @param query a string representing the free-text query.
   * @param parentGroupId the parent group to restrict the search to
   * @return a collection of GroupBean objects matching the query
   */
  Collection<GroupBean> searchGroups(String query, String parentGroupId);

  /**
   * Search for groups within the specified parent group, returning only the slice defined by {@code
   * limit} and {@code offset}.
   *
   * @param query The group name fragment to search for
   * @param parentGroupId The unique ID of the parent group to restrict the search to
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the matching groups for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<GroupBean> searchGroups(String query, String parentGroupId, int limit, int offset);

  /** Returns the parent group of the given group, or null if no parent exists. */
  GroupBean getParentGroupForGroup(String groupId);

  /**
   * Returns the total number of roles matching the given query.
   *
   * @param query The role name fragment to search for
   * @return Total number of matching roles
   */
  int countRoles(String query);

  /**
   * Perform a free-text search for roles.
   *
   * @param query a string representing the free-text query.
   * @return a collection of RoleBean objects matching the query
   */
  Collection<RoleBean> searchRoles(String query);

  /**
   * Search for roles, returning only the slice defined by {@code limit} and {@code offset}.
   *
   * @param query The role name fragment to search for
   * @param limit Maximum number of results to return (must be >= 0)
   * @param offset Zero-based start index of the first result to return (must be >= 0)
   * @return A ChainedResult with the matching roles for the requested page
   * @throws IllegalArgumentException if limit or offset is negative
   */
  ChainedResult<RoleBean> searchRoles(String query, int limit, int offset);

  /** Performs any final operations such as closing DB connections. */
  void close() throws Exception;

  /**
   * Used for remote applets.
   *
   * @param username The username to mangle
   * @return A token with a limited life span
   */
  String getGeneratedToken(String secretId, String username);

  List<String> getTokenSecretIds();

  void purgeFromCaches(String id);
}

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
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements all of the methods and returns null (*not* an empty list) or other meaningful return
 * value indicating there are no results. Subclasses should override the paginated/count methods
 * with native implementations where possible; the in-memory fallbacks provided here log a debug
 * message to make it easy to identify which plugins still rely on them.
 *
 * <p>The newer paginated and count methods return {@link ChainResult} instead of the legacy {@code
 * Pair<ChainDirective, Collection<T>>}. Unlike the legacy pair-based methods — which may return
 * {@code null} to signal "no result" — {@link ChainResult} should always have a non-null collection
 * (an empty collection is returned when there are no results).
 *
 * <p>TODO： OEQ-2943 replace Pair<ChainDirective, Collection<T>> with ChainResult
 */
public abstract class AbstractUserDirectory implements UserDirectory {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserDirectory.class);

  private UserDirectoryChain head;

  @Override
  public final boolean initialise(UserDirectoryChain head, UserManagementSettings settings) {
    this.head = head;
    return initialise(settings);
  }

  /**
   * @return true if the settings should be re-saved to the DB after initialisation.
   */
  protected abstract boolean initialise(UserManagementSettings settings);

  protected UserDirectoryChain getChain() {
    return head;
  }

  @Override
  public void initUserState(ModifiableUserState state) {
    // Nothing to do
  }

  @Override
  public ModifiableUserState authenticateUser(String username, String password) {
    return null;
  }

  @Override
  public ModifiableUserState authenticateUserFromUsername(String username, String privateData) {
    return null;
  }

  @Override
  public ModifiableUserState authenticateToken(String token) {
    return null;
  }

  @Override
  public ModifiableUserState authenticateRequest(HttpServletRequest request) {
    return null;
  }

  @Override
  public void initGuestUserState(ModifiableUserState state) {
    // Nothing to do
  }

  @Override
  public void initSystemUserState(ModifiableUserState state) {
    // nothing by default
  }

  @Override
  public VerifyTokenResult verifyUserStateForToken(UserState userState, String token) {
    return VerifyTokenResult.PASS;
  }

  @Override
  public void keepAlive() {
    // Nothing to do
  }

  @Override
  public void logout(UserState state) {
    // Nothing to do
  }

  @Override
  public UserBean getInformationForUser(String userID) {
    return null;
  }

  @Override
  public Map<String, UserBean> getInformationForUsers(Collection<String> userIDs) {
    return null;
  }

  @Override
  public GroupBean getInformationForGroup(String groupID) {
    return null;
  }

  @Override
  public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs) {
    return null;
  }

  @Override
  public RoleBean getInformationForRole(String roleID) {
    return null;
  }

  @Override
  public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs) {
    return null;
  }

  @Override
  public Pair<ChainDirective, Collection<RoleBean>> getRolesForUser(String userID) {
    return null;
  }

  @Override
  public Pair<ChainDirective, Collection<GroupBean>> getGroupsContainingUser(String userID) {
    return null;
  }

  @Override
  public int countUsersInGroup(String groupId, boolean recursive) {
    return fallbackCount("countUsersInGroup", getUsersInGroup(groupId, recursive));
  }

  @Override
  public ChainResult<UserBean> getUsersInGroup(
      String groupId, boolean recursive, int limit, int offset) {
    return fallbackPage("getUsersInGroup", getUsersInGroup(groupId, recursive), limit, offset);
  }

  @Override
  public Pair<ChainDirective, Collection<UserBean>> getUsersInGroup(
      String groupId, boolean recursive) {
    // Default behaviour for existing wrappers.
    Check.checkNotEmpty(groupId);
    return searchUsers(null, groupId, recursive);
  }

  @Override
  public int countUsers(String query) {
    return fallbackCount("countUsers", searchUsers(query));
  }

  @Override
  public int countUsers(String query, String parentGroupId, boolean recursive) {
    return fallbackCount("countUsers", searchUsers(query, parentGroupId, recursive));
  }

  @Override
  public Pair<ChainDirective, Collection<UserBean>> searchUsers(String query) {
    // Default behaviour for existing wrappers.
    return searchUsers(query, null, false);
  }

  @Override
  public Pair<ChainDirective, Collection<UserBean>> searchUsers(
      String query, String parentGroupID, boolean recursive) {
    return null;
  }

  @Override
  public ChainResult<UserBean> searchUsers(String query, int limit, int offset) {
    return fallbackPage("searchUsers", searchUsers(query), limit, offset);
  }

  @Override
  public ChainResult<UserBean> searchUsers(
      String query, String parentGroupId, boolean recursive, int limit, int offset) {
    return fallbackPage("searchUsers", searchUsers(query, parentGroupId, recursive), limit, offset);
  }

  @Override
  public int countGroups(String query) {
    return fallbackCount("countGroups", searchGroups(query));
  }

  @Override
  public int countGroups(String query, String parentGroupId) {
    return fallbackCount("countGroups", searchGroups(query, parentGroupId));
  }

  @Override
  public Collection<GroupBean> searchGroups(String query) {
    return null;
  }

  @Override
  public Collection<GroupBean> searchGroups(String query, String parentId) {
    return null;
  }

  @Override
  public ChainResult<GroupBean> searchGroups(String query, int limit, int offset) {
    return fallbackPage("searchGroups", searchGroups(query), limit, offset);
  }

  @Override
  public ChainResult<GroupBean> searchGroups(
      String query, String parentGroupId, int limit, int offset) {
    return fallbackPage("searchGroups", searchGroups(query, parentGroupId), limit, offset);
  }

  @Override
  public GroupBean getParentGroupForGroup(String groupID) {
    return null;
  }

  @Override
  public int countRoles(String query) {
    return fallbackCount("countRoles", searchRoles(query));
  }

  @Override
  public ChainResult<RoleBean> searchRoles(String query, int limit, int offset) {
    return fallbackPage("searchRoles", searchRoles(query), limit, offset);
  }

  @Override
  public Collection<RoleBean> searchRoles(String query) {
    return null;
  }

  @Override
  public void close() throws Exception {
    // Nothing to do
  }

  @Override
  public String getGeneratedToken(String secretId, String username) {
    return null;
  }

  @Override
  public List<String> getTokenSecretIds() {
    return null;
  }

  @Override
  public void purgeFromCaches(String id) {}

  /**
   * Returns a sub-list of {@code list} with at most {@code limit} elements starting at {@code
   * offset}.
   */
  public static <T> List<T> getSubList(List<T> list, int limit, int offset) {
    return list.stream().skip(offset).limit(limit).toList();
  }

  private <T> List<T> getResultList(Pair<ChainDirective, Collection<T>> pair) {
    return Optional.ofNullable(pair.getSecond()).<List<T>>map(ArrayList::new).orElseGet(List::of);
  }

  /**
   * Counts results from a legacy {@link Pair} result collection.
   *
   * <p>Returns 0 when the legacy method returns {@code null}, either because it is not implemented
   * by the plugin or because existing legacy code intentionally uses {@code null} to indicate no
   * contributed results.
   */
  private <T> int fallbackCount(String method, Pair<ChainDirective, Collection<T>> result) {
    if (result == null) {
      return 0;
    }
    logFallbackCount(method);
    return getResultList(result).size();
  }

  /**
   * Counts results from a {@link Collection} result.
   *
   * <p>Returns 0 when the legacy method returns {@code null}, either because it is not implemented
   * by the plugin or because existing legacy code intentionally uses {@code null} to indicate no
   * contributed results.
   */
  private <T> int fallbackCount(String method, Collection<T> result) {
    if (result == null) {
      return 0;
    }
    logFallbackCount(method);
    return result.size();
  }

  /**
   * Applies fallback in-memory pagination to a legacy {@link Pair} result collection.
   *
   * <p>Returns an empty continuing {@link ChainResult} when the legacy method returns {@code null},
   * either because it is not implemented by the plugin or because existing legacy code
   * intentionally uses {@code null} to indicate no contributed results.
   */
  private <T> ChainResult<T> fallbackPage(
      String method, Pair<ChainDirective, Collection<T>> result, int limit, int offset) {
    if (result == null) {
      return ChainResult.continueWithEmpty();
    }
    logFallbackPaging(method, limit, offset);
    return paginateBySlicing(getResultList(result), limit, offset);
  }

  /**
   * Applies fallback in-memory pagination to a full result collection.
   *
   * <p>Returns an empty continuing {@link ChainResult} when the legacy method returns {@code null},
   * either because it is not implemented by the plugin or because existing legacy code
   * intentionally uses {@code null} to indicate no contributed results.
   */
  private <T> ChainResult<T> fallbackPage(
      String method, Collection<T> result, int limit, int offset) {
    if (result == null) {
      return ChainResult.continueWithEmpty();
    }
    logFallbackPaging(method, limit, offset);
    return paginateBySlicing(new ArrayList<>(result), limit, offset);
  }

  private <T> ChainResult<T> paginateBySlicing(List<T> source, int limit, int offset) {
    return ChainResult.continueWith(getSubList(source, limit, offset));
  }

  private void logFallbackCount(String method) {
    LOGGER.debug(
        "[{}] {}: falling back to in-memory count", this.getClass().getSimpleName(), method);
  }

  private void logFallbackPaging(String method, int limit, int offset) {
    LOGGER.debug(
        "[{}] {}: falling back to in-memory paging (limit={}, offset={})",
        this.getClass().getSimpleName(),
        method,
        limit,
        offset);
  }
}

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

import static com.tle.plugins.ump.UserDirectoryUtils.makeCache;
import static com.tle.plugins.ump.UserDirectoryUtils.makeShortLivedCache;

import com.dytech.edge.common.Constants;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.CacheUtils;
import com.tle.core.institution.RunAsInstitution;
import com.tle.plugins.ump.UserDirectory.VerifyTokenResult;
import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDirectoryChainImpl implements UserDirectoryChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserDirectoryChainImpl.class);

  // Supplies institution/user context restoration for concurrently executed user-directory counts.
  private final RunAsInstitution runAs;

  private static final UserBean USER_NOT_FOUND = new EmptyUserBean();
  private static final RoleBean ROLE_NOT_FOUND = new EmptyRoleBean();
  private static final GroupBean GROUP_NOT_FOUND = new EmptyGroupBean();

  private final Cache<String, UserBean> userCache = makeCache();
  // Role config changes recreate the whole UserDirectoryChain, so this cache does not need local
  // invalidation logic for role-setting updates.
  private final Cache<String, RoleBean> roleCache = makeCache();
  private final Cache<String, GroupBean> groupCache = makeCache();
  private final Cache<String, List<GroupBean>> groupsContainingUserCache = makeCache();
  private final Cache<String, List<UserBean>> searchUsersCache = makeCache();
  private final Cache<String, List<GroupBean>> searchGroupsCache = makeShortLivedCache();
  private final Cache<String, List<UserBean>> getUsersInGroupCache = makeShortLivedCache();
  // Same rationale as roleCache: role updates rebuild the chain, so cached role search results are
  // naturally replaced.
  private final Cache<String, List<RoleBean>> searchRolesCache = makeShortLivedCache();

  private List<UserDirectory> uds;

  public UserDirectoryChainImpl(RunAsInstitution runAs) {
    this.runAs = runAs;
  }

  public void setChain(List<UserDirectory> uds) {
    this.uds = uds;
  }

  @Override
  public void purgeUserFromCaches(String id) {
    LOGGER.debug("Purging user cache with key {}", id);

    for (UserDirectory ud : uds) {
      ud.purgeFromCaches(id);
    }
    userCache.invalidate(id);

    groupsContainingUserCache.invalidate(id);
    // Short-lived cache. Invalidate all entries rather than attempting to
    // determine every affected group/recursive/page combination.
    getUsersInGroupCache.invalidateAll();
    // Search user cache keys include the query and optional group scope, so the affected keys
    // cannot be reliably identified from a user id alone.
    searchUsersCache.invalidateAll();
  }

  @Override
  public void purgeGroupFromCaches(String groupId) {
    LOGGER.debug("Purging group cache with key {}", groupId);

    groupCache.invalidate(groupId);
    // Short-lived cache. Invalidate all entries rather than attempting to
    // determine every affected group/recursive/page combination.
    getUsersInGroupCache.invalidateAll();
    // Short-lived cache. The search group cache keys include the query and optional group scope,
    // so the affected keys cannot be reliably identified from a group id alone.
    searchGroupsCache.invalidateAll();
    // Search user cache keys include the query and optional group scope, so the affected keys
    // cannot be reliably identified from a group id alone.
    searchUsersCache.invalidateAll();

    // user is deleted from a group
    Iterator<Entry<String, List<GroupBean>>> it =
        groupsContainingUserCache.asMap().entrySet().iterator();
    while (it.hasNext() && groupsContainingUserCache.size() > 0) {
      Entry<String, List<GroupBean>> cacheValues = it.next();
      for (GroupBean group : cacheValues.getValue()) {
        if (group.getUniqueID().equals(groupId)) {
          groupsContainingUserCache.invalidate(cacheValues.getKey());
        }
      }
    }
    for (UserDirectory ud : uds) {
      ud.purgeFromCaches(groupId);
    }
  }

  @Override
  public ModifiableUserState authenticateRequest(HttpServletRequest request) {
    for (UserDirectory ud : uds) {
      ModifiableUserState mus = ud.authenticateRequest(request);
      if (mus != null) {
        return mus;
      }
    }
    return null;
  }

  @Override
  public ModifiableUserState authenticateToken(String token) {
    for (UserDirectory ud : uds) {
      ModifiableUserState mus = ud.authenticateToken(token);
      if (mus != null) {
        return mus;
      }
    }
    return null;
  }

  @Override
  public ModifiableUserState authenticateUser(String username, String password) {
    for (UserDirectory ud : uds) {
      ModifiableUserState mus = ud.authenticateUser(username, password);
      if (mus != null) {
        return mus;
      }
    }
    return null;
  }

  @Override
  public ModifiableUserState authenticateUserFromUsername(String username, String privateData) {
    for (UserDirectory ud : uds) {
      ModifiableUserState mus = ud.authenticateUserFromUsername(username, privateData);
      if (mus != null) {
        return mus;
      }
    }
    return null;
  }

  @Override
  public void close() throws Exception {
    for (UserDirectory ud : uds) {
      ud.close();
    }
  }

  @Override
  public String getGeneratedToken(String secretId, String username) {
    for (UserDirectory ud : uds) {
      String token = ud.getGeneratedToken(secretId, username);
      if (token != null) {
        return token;
      }
    }
    return null;
  }

  @Override
  public List<GroupBean> getGroupsContainingUser(String userId) {
    LOGGER.debug("Getting groups containing user '{}'", userId);

    if (Check.isEmpty(userId)) {
      return Collections.emptyList();
    }
    return getFromCacheOrLoadList(
        groupsContainingUserCache, userId, () -> fetchGroupsContainingUser(userId));
  }

  @Override
  public List<String> getGroupIdsContainingUser(String userId) {
    return getGroupsContainingUser(userId).stream().map(GroupBean::getUniqueID).toList();
  }

  private List<GroupBean> fetchGroupsContainingUser(String userId) {
    List<GroupBean> rv = null;
    for (UserDirectory ud : uds) {
      Pair<ChainDirective, Collection<GroupBean>> gcu = ud.getGroupsContainingUser(userId);
      if (gcu != null) {
        rv = accumulate(rv, gcu.getSecond());
        if (gcu.getFirst() == ChainDirective.STOP) {
          break;
        }
      }
    }
    return rv;
  }

  @Override
  public int countUsersInGroup(String groupId, boolean recursive) {
    return countAll(ud -> ud.countUsersInGroup(groupId, recursive));
  }

  private List<UserBean> fetchUsersInGroup(String groupId, boolean recursive) {
    List<UserBean> rv = null;
    for (UserDirectory ud : uds) {
      Pair<ChainDirective, Collection<UserBean>> ufg = ud.getUsersInGroup(groupId, recursive);
      if (ufg != null) {
        rv = accumulate(rv, ufg.getSecond());
        if (ufg.getFirst() == ChainDirective.STOP) {
          break;
        }
      }
    }
    return nullToEmpty(rv);
  }

  @Override
  public List<UserBean> getUsersInGroup(String groupId, boolean recursive) {
    LOGGER.debug("Getting users in group '{}', recursive '{}'", groupId, recursive);

    Check.checkNotEmpty(groupId);
    return getFromCacheOrLoadList(
        getUsersInGroupCache,
        CacheUtils.buildCacheKey(groupId, recursive),
        () -> fetchUsersInGroup(groupId, recursive));
  }

  @Override
  public List<UserBean> getUsersInGroup(String groupId, boolean recursive, int limit, int offset) {
    LOGGER.debug(
        "Getting users in group '{}', recursive '{}', limit '{}', offset '{}'",
        groupId,
        recursive,
        limit,
        offset);
    final String cacheKey = CacheUtils.buildCacheKey(groupId, recursive, limit, offset);
    final Supplier<List<UserBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countUsersInGroup(groupId, recursive),
                    (ud, lim, off) -> ud.getUsersInGroup(groupId, recursive, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(getUsersInGroupCache, cacheKey, loader);
  }

  @Override
  public GroupBean getInformationForGroup(String groupId) {
    if (groupId == null) {
      return null;
    }

    GroupBean cgb = groupCache.getIfPresent(groupId);
    if (cgb != null) {
      return cgb.equals(GROUP_NOT_FOUND) ? null : cgb;
    }

    for (UserDirectory ud : uds) {
      GroupBean gb = ud.getInformationForGroup(groupId);
      if (gb != null) {
        groupCache.put(groupId, gb);
        return gb;
      }
    }

    groupCache.put(groupId, GROUP_NOT_FOUND);
    return null;
  }

  @Override
  public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds) {
    if (Check.isEmpty(groupIds)) {
      return Collections.emptyMap();
    }

    Set<String> gids = new HashSet<String>(groupIds);
    gids.remove(null);
    gids.remove(Constants.BLANK);

    Map<String, GroupBean> rv = Maps.newHashMapWithExpectedSize(gids.size());

    // Populate from the cache first
    for (String groupId : gids) {
      GroupBean cgb = groupCache.getIfPresent(groupId);
      if (cgb != null && !cgb.equals(GROUP_NOT_FOUND)) {
        rv.put(groupId, cgb);
      }
    }

    for (UserDirectory ud : uds) {
      Map<String, GroupBean> found = ud.getInformationForGroups(gids);
      if (!Check.isEmpty(found)) {
        gids.removeAll(found.keySet());

        rv.putAll(found);
        groupCache.putAll(found);
      }

      if (gids.isEmpty()) {
        break;
      }
    }

    // Make sure we mark invalid/unfound ids in the cache
    for (String groupId : gids) {
      groupCache.put(groupId, GROUP_NOT_FOUND);
    }

    return rv;
  }

  @Override
  public RoleBean getInformationForRole(String roleId) {
    if (roleId == null) {
      return null;
    }

    RoleBean cub = roleCache.getIfPresent(roleId);
    if (cub != null) {
      return cub.equals(ROLE_NOT_FOUND) ? null : cub;
    }

    for (UserDirectory ud : uds) {
      RoleBean rb = ud.getInformationForRole(roleId);
      if (rb != null) {
        roleCache.put(roleId, rb);
        return rb;
      }
    }

    roleCache.put(roleId, ROLE_NOT_FOUND);
    return null;
  }

  @Override
  public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds) {
    if (Check.isEmpty(roleIds)) {
      return Collections.emptyMap();
    }

    Set<String> rids = new HashSet<String>(roleIds);
    rids.remove(null);
    rids.remove(Constants.BLANK);

    Map<String, RoleBean> rv = Maps.newHashMapWithExpectedSize(rids.size());

    // Populate from the cache first
    for (String roleId : rids) {
      RoleBean crb = roleCache.getIfPresent(roleId);
      if (crb != null && !crb.equals(ROLE_NOT_FOUND)) {
        rv.put(roleId, crb);
      }
    }

    for (UserDirectory ud : uds) {
      Map<String, RoleBean> found = ud.getInformationForRoles(rids);
      if (!Check.isEmpty(found)) {
        rids.removeAll(found.keySet());

        rv.putAll(found);
        roleCache.putAll(found);
      }

      if (rids.isEmpty()) {
        break;
      }
    }

    // Make sure we mark invalid/unfound ids in the cache
    for (String roleId : rids) {
      roleCache.put(roleId, ROLE_NOT_FOUND);
    }

    return rv;
  }

  @Override
  public UserBean getInformationForUser(String userId) {
    if (userId == null) {
      return null;
    }

    UserBean cub = userCache.getIfPresent(userId);
    if (cub != null) {
      return cub.equals(USER_NOT_FOUND) ? null : cub;
    }

    for (UserDirectory ud : uds) {
      UserBean ub = ud.getInformationForUser(userId);
      if (ub != null) {
        userCache.put(userId, ub);
        return ub;
      }
    }

    userCache.put(userId, USER_NOT_FOUND);
    return null;
  }

  @Override
  public Map<String, UserBean> getInformationForUsers(Collection<String> userIds) {
    if (Check.isEmpty(userIds)) {
      return Collections.emptyMap();
    }

    Set<String> uids = new HashSet<String>(userIds);
    uids.remove(null);
    uids.remove(Constants.BLANK);

    Map<String, UserBean> rv = Maps.newHashMapWithExpectedSize(uids.size());

    // Populate from the cache first
    for (String userId : uids) {
      UserBean cub = userCache.getIfPresent(userId);
      if (cub != null && !cub.equals(USER_NOT_FOUND)) {
        rv.put(userId, cub);
      }
    }

    // Maybe we got it all from the cache!
    uids.removeAll(rv.keySet());
    if (uids.isEmpty()) {
      return rv;
    }

    // Find any un-cached information
    for (UserDirectory ud : uds) {
      Map<String, UserBean> found = ud.getInformationForUsers(uids);
      if (!Check.isEmpty(found)) {
        uids.removeAll(found.keySet());

        rv.putAll(found);
        userCache.putAll(found);
      }

      if (uids.isEmpty()) {
        break;
      }
    }

    // Make sure we mark invalid/unfound ids in the cache
    for (String userId : uids) {
      userCache.put(userId, USER_NOT_FOUND);
    }

    return rv;
  }

  @Override
  public GroupBean getParentGroupForGroup(String groupId) {
    for (UserDirectory ud : uds) {
      GroupBean gb = ud.getParentGroupForGroup(groupId);
      if (gb != null) {
        return gb;
      }
    }
    return null;
  }

  @Override
  public List<RoleBean> getRolesForUser(String userId) {
    List<RoleBean> rv = null;
    for (UserDirectory ud : uds) {
      Pair<ChainDirective, Collection<RoleBean>> rfu = ud.getRolesForUser(userId);
      if (rfu != null) {
        rv = accumulate(rv, rfu.getSecond());
        if (rfu.getFirst() == ChainDirective.STOP) {
          break;
        }
      }
    }
    return nullToEmpty(rv);
  }

  @Override
  public List<String> getTokenSecretIds() {
    List<String> rv = null;
    for (UserDirectory ud : uds) {
      List<String> tokens = ud.getTokenSecretIds();
      rv = accumulate(rv, tokens);
    }
    return nullToEmpty(rv);
  }

  @Override
  public void initGuestUserState(ModifiableUserState state) {
    // Let the more authoritative user directories supply details first by
    // doing this in reverse.
    for (UserDirectory ud : Lists.reverse(uds)) {
      ud.initGuestUserState(state);
    }
  }

  @Override
  public void initUserState(ModifiableUserState state) {
    // Let the more authoritative user directories supply details first by
    // doing this in reverse.
    for (UserDirectory ud : Lists.reverse(uds)) {
      ud.initUserState(state);
    }
  }

  @Override
  public void initSystemUserState(ModifiableUserState state) {
    // Let the more authoritative user directories supply details first by
    // doing this in reverse.
    for (UserDirectory ud : Lists.reverse(uds)) {
      ud.initSystemUserState(state);
    }
  }

  @Override
  public void keepAlive() {
    for (UserDirectory ud : uds) {
      ud.keepAlive();
    }
  }

  @Override
  public void logout(UserState state) {
    for (UserDirectory ud : uds) {
      ud.logout(state);
    }
  }

  @Override
  public int countGroups(String query) {
    return countAll(ud -> ud.countGroups(query));
  }

  @Override
  public int countGroups(String query, String parentGroupId) {
    return countAll(ud -> ud.countGroups(query, parentGroupId));
  }

  private List<GroupBean> fetchSearchGroups(String query) {
    List<GroupBean> rv = null;
    for (UserDirectory ud : uds) {
      rv = accumulate(rv, ud.searchGroups(query));
    }
    return nullToEmpty(rv);
  }

  private List<GroupBean> fetchSearchGroups(String query, String parentId) {
    List<GroupBean> rv = null;
    for (UserDirectory ud : uds) {
      rv = accumulate(rv, ud.searchGroups(query, parentId));
    }
    return nullToEmpty(rv);
  }

  @Override
  public List<GroupBean> searchGroups(String query) {
    LOGGER.debug("Searching groups with query '{}'", query);
    return getFromCacheOrLoadList(searchGroupsCache, query, () -> fetchSearchGroups(query));
  }

  @Override
  public List<GroupBean> searchGroups(String query, String parentId) {
    LOGGER.debug("Searching groups with query '{}', parentId '{}'", query, parentId);
    return getFromCacheOrLoadList(
        searchGroupsCache,
        CacheUtils.buildCacheKey(query, parentId),
        () -> fetchSearchGroups(query, parentId));
  }

  @Override
  public List<GroupBean> searchGroups(String query, String parentGroupId, int limit, int offset) {
    LOGGER.debug(
        "Searching groups with query '{}', parentGroupId '{}', limit '{}', offset '{}'",
        query,
        parentGroupId,
        limit,
        offset);
    final String cacheKey = CacheUtils.buildCacheKey(query, parentGroupId, limit, offset);
    final Supplier<List<GroupBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countGroups(query, parentGroupId),
                    (ud, lim, off) -> ud.searchGroups(query, parentGroupId, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(searchGroupsCache, cacheKey, loader);
  }

  @Override
  public List<GroupBean> searchGroups(String query, int limit, int offset) {
    LOGGER.debug("Searching groups with query '{}', limit '{}', offset '{}'", query, limit, offset);
    final String cacheKey = CacheUtils.buildCacheKey(query, limit, offset);
    final Supplier<List<GroupBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countGroups(query),
                    (ud, lim, off) -> ud.searchGroups(query, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(searchGroupsCache, cacheKey, loader);
  }

  @Override
  public int countRoles(String query) {
    return countAll(ud -> ud.countRoles(query));
  }

  private List<RoleBean> fetchSearchRoles(String query) {
    List<RoleBean> rv = null;
    for (UserDirectory ud : uds) {
      rv = accumulate(rv, ud.searchRoles(query));
    }
    return nullToEmpty(rv);
  }

  @Override
  public List<RoleBean> searchRoles(String query) {
    LOGGER.debug("Searching roles with query '{}'", query);
    return getFromCacheOrLoadList(searchRolesCache, query, () -> fetchSearchRoles(query));
  }

  @Override
  public List<RoleBean> searchRoles(String query, int limit, int offset) {
    LOGGER.debug("Searching roles with query '{}', limit '{}', offset '{}'", query, limit, offset);
    final String cacheKey = CacheUtils.buildCacheKey(query, limit, offset);
    final Supplier<List<RoleBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countRoles(query), (ud, lim, off) -> ud.searchRoles(query, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(searchRolesCache, cacheKey, loader);
  }

  @Override
  public int countUsers(String query) {
    return countAll(ud -> ud.countUsers(query));
  }

  @Override
  public int countUsers(String query, String parentGroupId, boolean recursive) {
    return countAll(ud -> ud.countUsers(query, parentGroupId, recursive));
  }

  private List<UserBean> fetchSearchUsers(
      String query, String parentGroupId, boolean noGroupId, boolean recursive) {
    List<UserBean> rv = null;
    for (UserDirectory ud : uds) {
      Pair<ChainDirective, Collection<UserBean>> results =
          noGroupId ? ud.searchUsers(query) : ud.searchUsers(query, parentGroupId, recursive);
      if (results != null) {
        rv = accumulate(rv, results.getSecond());
        if (results.getFirst() == ChainDirective.STOP) {
          break;
        }
      }
    }
    return rv;
  }

  @Override
  public List<UserBean> searchUsers(final String query) {
    // The more complex version of this method already contains the logic
    // for ignoring the empty parent group ID and calling this version on
    // the chain.
    return searchUsers(query, null, false);
  }

  @Override
  public List<UserBean> searchUsers(String query, String parentGroupId, boolean recursive) {
    LOGGER.debug(
        "Searching users with query '{}', parentGroupId '{}', recursive '{}'",
        query,
        parentGroupId,
        recursive);
    boolean noGroupId = Check.isEmpty(parentGroupId);
    final String cacheKey =
        noGroupId ? query : CacheUtils.buildCacheKey(query, parentGroupId, recursive);
    return getFromCacheOrLoadList(
        searchUsersCache,
        cacheKey,
        () -> fetchSearchUsers(query, parentGroupId, noGroupId, recursive));
  }

  @Override
  public List<UserBean> searchUsers(String query, int limit, int offset) {
    LOGGER.debug("Searching users with query '{}', limit '{}', offset '{}'", query, limit, offset);
    final String cacheKey = CacheUtils.buildCacheKey(query, limit, offset);
    final Supplier<List<UserBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countUsers(query), (ud, lim, off) -> ud.searchUsers(query, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(searchUsersCache, cacheKey, loader);
  }

  @Override
  public List<UserBean> searchUsers(
      String query, String parentGroupId, boolean recursive, int limit, int offset) {
    LOGGER.debug(
        "Searching users with query '{}', parentGroupId '{}', recursive '{}', "
            + "limit '{}', offset '{}'",
        query,
        parentGroupId,
        recursive,
        limit,
        offset);
    final String cacheKey =
        CacheUtils.buildCacheKey(query, parentGroupId, recursive, limit, offset);
    final Supplier<List<UserBean>> loader =
        () ->
            fetchPage(
                new PagingFunctions<>(
                    ud -> ud.countUsers(query, parentGroupId, recursive),
                    (ud, lim, off) -> ud.searchUsers(query, parentGroupId, recursive, lim, off)),
                new PageRange(limit, offset));
    return getFromCacheOrLoadList(searchUsersCache, cacheKey, loader);
  }

  @Override
  public boolean verifyUserStateForToken(UserState userState, String token) {
    for (UserDirectory ud : uds) {
      VerifyTokenResult vtr = ud.verifyUserStateForToken(userState, token);
      if (vtr != VerifyTokenResult.PASS) {
        return vtr == VerifyTokenResult.VALID;
      }
    }
    return false;
  }

  /** Returns the cached list for {@code key} if present; otherwise invokes {@code loader}. */
  private <T> List<T> getFromCacheOrLoadList(
      Cache<String, List<T>> cache, String key, Supplier<List<T>> loader) {
    try {
      return cache.get(
          key,
          () -> {
            LOGGER.debug("Cache miss for key '{}', loading from chain", key);
            return emptyOrUnmodifiable(loader.get());
          });
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to load cache value for key: " + key, e);
    }
  }

  private static <U> List<U> accumulate(List<U> rv, Collection<U> newValues) {
    if (!Check.isEmpty(newValues)) {
      if (rv == null) {
        rv = Lists.newArrayListWithCapacity(newValues.size());
      }
      rv.addAll(newValues);
    }
    return rv;
  }

  private static <U> List<U> nullToEmpty(List<U> rv) {
    if (rv == null) {
      rv = Collections.emptyList();
    }
    return rv;
  }

  private static <U> List<U> emptyOrUnmodifiable(List<U> rv) {
    if (rv == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(rv);
    }
  }

  private static final class EmptyUserBean implements UserBean {
    @Serial private static final long serialVersionUID = 1L;

    @Override
    public String getUniqueID() {
      throw new IllegalStateException();
    }

    @Override
    public String getUsername() {
      throw new IllegalStateException();
    }

    @Override
    public String getFirstName() {
      throw new IllegalStateException();
    }

    @Override
    public String getLastName() {
      throw new IllegalStateException();
    }

    @Override
    public String getEmailAddress() {
      throw new IllegalStateException();
    }
  }

  private static final class EmptyGroupBean implements GroupBean {
    @Serial private static final long serialVersionUID = 1L;

    @Override
    public String getUniqueID() {
      throw new IllegalStateException();
    }

    @Override
    public String getName() {
      throw new IllegalStateException();
    }
  }

  private static final class EmptyRoleBean implements RoleBean {
    @Serial private static final long serialVersionUID = 1L;

    @Override
    public String getUniqueID() {
      throw new IllegalStateException();
    }

    @Override
    public String getName() {
      throw new IllegalStateException();
    }
  }

  private int countAll(ToIntFunction<UserDirectory> countFunction) {
    return new ChainCounter(uds, runAs).count(countFunction);
  }

  private <T> List<T> fetchPage(PagingFunctions<T> pagingFunctions, PageRange pageRange) {
    return new ChainPager<>(uds, pagingFunctions).getPage(pageRange);
  }
}

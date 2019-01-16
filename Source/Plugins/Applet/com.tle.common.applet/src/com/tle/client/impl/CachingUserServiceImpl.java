/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.client.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

public class CachingUserServiceImpl implements RemoteUserService {
  private static final Cache<String, UserBean> USER_CACHE =
      CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
  private static final Cache<String, GroupBean> GROUP_CACHE =
      CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
  private static final Cache<String, RoleBean> ROLE_CACHE =
      CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

  private final RemoteUserService remoteUserService;

  public CachingUserServiceImpl(RemoteUserService remoteUserService) {
    this.remoteUserService = remoteUserService;
  }

  @Override
  public Map<String, UserBean> getInformationForUsers(Collection<String> userUniqueIDs) {
    return remoteUserService.getInformationForUsers(userUniqueIDs);
  }

  @Override
  public UserBean getInformationForUser(String userid) {
    UserBean userInfo = USER_CACHE.getIfPresent(userid);
    if (userInfo == null) {
      userInfo = remoteUserService.getInformationForUser(userid);
      if (userInfo != null) {
        USER_CACHE.put(userid, userInfo);
      }
    }
    return userInfo;
  }

  @Override
  public List<RoleBean> getRolesForUser(String userid) {
    return remoteUserService.getRolesForUser(userid);
  }

  @Override
  public List<String> getGroupIdsContainingUser(String userid) {
    return remoteUserService.getGroupIdsContainingUser(userid);
  }

  @Override
  public List<UserBean> getUsersInGroup(String groupId, boolean recursive) {
    return remoteUserService.getUsersInGroup(groupId, recursive);
  }

  @Override
  public List<GroupBean> getGroupsContainingUser(String userid) {
    return remoteUserService.getGroupsContainingUser(userid);
  }

  @Override
  public List<UserBean> searchUsers(String query) {
    return remoteUserService.searchUsers(query);
  }

  @Override
  public List<UserBean> searchUsers(String query, String parentGroupID, boolean recurse) {
    return remoteUserService.searchUsers(query, parentGroupID, recurse);
  }

  @Override
  public GroupBean getInformationForGroup(String uuid) {
    GroupBean groupInfo = GROUP_CACHE.getIfPresent(uuid);
    if (groupInfo == null) {
      groupInfo = remoteUserService.getInformationForGroup(uuid);
      if (groupInfo != null) {
        GROUP_CACHE.put(uuid, groupInfo);
      }
    }
    return groupInfo;
  }

  @Override
  public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs) {
    return remoteUserService.getInformationForGroups(groupIDs);
  }

  @Override
  public List<GroupBean> searchGroups(String query) {
    return remoteUserService.searchGroups(query);
  }

  @Override
  public List<GroupBean> searchGroups(String query, String parentId) {
    return remoteUserService.searchGroups(query, parentId);
  }

  @Override
  public RoleBean getInformationForRole(String uuid) {
    RoleBean roleInfo = ROLE_CACHE.getIfPresent(uuid);
    if (roleInfo == null) {
      roleInfo = remoteUserService.getInformationForRole(uuid);
      if (roleInfo != null) {
        ROLE_CACHE.put(uuid, roleInfo);
      }
    }

    return roleInfo;
  }

  @Override
  public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs) {
    return remoteUserService.getInformationForRoles(roleIDs);
  }

  @Override
  public List<RoleBean> searchRoles(String query) {
    return remoteUserService.searchRoles(query);
  }

  @Override
  public GroupBean getParentGroupForGroup(String groupID) {
    return remoteUserService.getParentGroupForGroup(groupID);
  }

  @Override
  public void keepAlive() {
    remoteUserService.keepAlive();
  }

  @Override
  public List<String> getTokenSecretIds() {
    return remoteUserService.getTokenSecretIds();
  }

  @Override
  public UserManagementSettings getPluginConfig(String settingsConfig) {
    return remoteUserService.getPluginConfig(settingsConfig);
  }

  @Override
  public UserManagementSettings getReadOnlyPluginConfig(String settingsConfig) {
    return remoteUserService.getReadOnlyPluginConfig(settingsConfig);
  }

  @Override
  public void setPluginConfig(UserManagementSettings config) {
    remoteUserService.setPluginConfig(config);
  }

  @Override
  public void removeFromCache(String userid) {
    USER_CACHE.invalidate(userid);
  }
}

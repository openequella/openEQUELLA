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

import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * This interface has many similarities with {@link UserDirectory}, but with a couple of
 * differences.
 *
 * <ol>
 *   <li>Some method results are just lists rather than pair containing a chain control value and
 *       list. The chain control stuff is only interesting to the chain handler itself, not to other
 *       objects using the chain.
 *   <li>Unlike UserDirectory implementations, any returned lists or maps from the chain must not be
 *       null.
 * </ol>
 */
public interface UserDirectoryChain extends UserDirectoryQueries {
  /**
   * Purges all cached entries associated with the given user.
   *
   * @param id the unique ID of the user.
   */
  void purgeUserFromCaches(String id);

  /**
   * Purges all cached entries associated with the given {@code groupId}. This includes the group
   * info cache, group-membership cache, and all group/role search result caches.
   *
   * @param groupId the unique ID of the group.
   */
  void purgeGroupFromCaches(String groupId);

  ModifiableUserState authenticateToken(String token);

  ModifiableUserState authenticateUser(String username, String password);

  ModifiableUserState authenticateUserFromUsername(String username, String privateData);

  ModifiableUserState authenticateRequest(HttpServletRequest request);

  void close() throws Exception;

  String getGeneratedToken(String secretId, String username);

  List<String> getTokenSecretIds();

  void initGuestUserState(ModifiableUserState state);

  void initUserState(ModifiableUserState state);

  void initSystemUserState(ModifiableUserState state);

  void logout(UserState state);

  boolean verifyUserStateForToken(UserState userState, String token);
}

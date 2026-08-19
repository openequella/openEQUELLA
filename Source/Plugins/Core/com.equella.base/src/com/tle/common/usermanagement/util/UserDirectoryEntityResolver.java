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

package com.tle.common.usermanagement.util;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.Optional;

/**
 * Minimal by-ID resolver extracted for {@link UserBeanUtils}.
 *
 * <p>This interface lives in {@code com.equella.base} because {@link UserBeanUtils} is shared by
 * both the Admin Console ({@code com.equella.admin}) and Core ({@code com.equella.core}). Keeping
 * it here avoids a circular dependency: neither plugin can depend on the other, but both can depend
 * on {@code com.equella.base}.
 */
public interface UserDirectoryEntityResolver {
  /**
   * Get information for the specified user.
   *
   * @param userId The unique ID of the user to get
   * @return The user if it exists, or empty if it does not or the API reports it as not found
   */
  Optional<UserBean> getInformationForUser(String userId);

  /**
   * Resolves a single group ID.
   *
   * @param groupId The unique ID of the group to get
   * @return The group if it exists, or empty if it does not or the API reports it as not found
   */
  Optional<GroupBean> getInformationForGroup(String groupId);

  /**
   * Resolves a single role ID.
   *
   * @param roleId The unique ID of the role to get
   * @return The role if it exists, or empty if it does not or the API reports it as not found
   */
  Optional<RoleBean> getInformationForRole(String roleId);
}

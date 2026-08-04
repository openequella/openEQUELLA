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

package com.tle.core.services.user;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.util.UserDirectoryEntityResolver;
import java.util.Optional;

/**
 * Adapts a {@link UserService} (which returns {@code null} for missing entities) into the {@link
 * UserDirectoryEntityResolver} interface (which returns {@link Optional}).
 *
 * <p>This adapter exists because {@link UserService} pre-dates {@link Optional} and its null-based
 * contract is used pervasively in Core. Rather than changing {@link UserService} and all existing
 * callers, this class acts as a bridge so that {@link
 * com.tle.common.usermanagement.util.UserBeanUtils} — which is shared between Core and the Admin
 * Console — can work with both sides via a single interface.
 */
public class UserServiceEntityResolverAdapter implements UserDirectoryEntityResolver {
  private final UserService userService;

  public UserServiceEntityResolverAdapter(UserService userService) {
    this.userService = userService;
  }

  /**
   * Delegates user lookup to {@link UserService} and converts a null result to {@link
   * Optional#empty()}.
   */
  @Override
  public Optional<UserBean> getInformationForUser(String userId) {
    return Optional.ofNullable(userService.getInformationForUser(userId));
  }

  /**
   * Delegates group lookup to {@link UserService} and converts a null result to {@link
   * Optional#empty()}.
   */
  @Override
  public Optional<GroupBean> getInformationForGroup(String groupId) {
    return Optional.ofNullable(userService.getInformationForGroup(groupId));
  }

  /**
   * Delegates role lookup to {@link UserService} and converts a null result to {@link
   * Optional#empty()}.
   */
  @Override
  public Optional<RoleBean> getInformationForRole(String roleId) {
    return Optional.ofNullable(userService.getInformationForRole(roleId));
  }
}

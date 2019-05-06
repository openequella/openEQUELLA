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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.Map;
import java.util.Set;

public class LazyUserLookup {
  private Set<String> usersToLookup = Sets.newHashSet();
  private Map<String, UserBean> lookedUpUsers = Maps.newHashMap();
  private UserService userService;

  public LazyUserLookup(UserService userService) {
    this.userService = userService;
  }

  public void addUser(String userUuid) {
    usersToLookup.add(userUuid);
  }

  public UserBean get(String userUuid) {
    if (usersToLookup.contains(userUuid)) {
      lookedUpUsers.putAll(userService.getInformationForUsers(usersToLookup));
      usersToLookup.clear();
    }
    return lookedUpUsers.get(userUuid);
  }
}

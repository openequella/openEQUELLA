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

import com.tle.beans.user.TLEUser;
import java.util.List;

public interface RemoteTLEUserService {
  String add(TLEUser newUser);

  String add(TLEUser newUser, boolean passwordNotHashed);

  String add(TLEUser newUser, List<String> groups);

  String add(String username, List<String> groups);

  TLEUser get(String id);

  TLEUser getByUsername(String username);

  /**
   * Given an existing user's TLEUser entity which has been modified, update the user in the
   * database.
   *
   * @param user The user to update
   * @param passwordNotHashed Whether the password is already hashed - if not, validate it meets
   *     password requirements and hash it before updating the user.
   * @return The UUID of the updated user
   */
  String edit(TLEUser user, boolean passwordNotHashed);

  void delete(String uuid);

  List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive);

  List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive, Integer limit);

  List<TLEUser> searchUsers(
      String query, String parentGroupID, boolean recursive, Integer limit, Integer offset);

  /**
   * Count the number of users in the system.
   *
   * @return The number of users in the system
   */
  int countUsers();

  /**
   * Count the number of users in the system that match the given query.
   *
   * @see #searchUsers(String, String, boolean)
   * @see #countUsers()
   */
  int countUsers(String query, String parentGroupID, boolean recursive);
}

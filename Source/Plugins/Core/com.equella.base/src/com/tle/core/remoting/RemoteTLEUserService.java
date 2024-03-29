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
import java.util.Set;

public interface RemoteTLEUserService {
  String add(TLEUser newUser);

  String add(TLEUser newUser, boolean passwordNotHashed);

  String add(TLEUser newUser, List<String> groups);

  String add(String username, List<String> groups);

  TLEUser get(String id);

  TLEUser getByUsername(String username);

  String edit(TLEUser user, boolean passwordNotHashed);

  void delete(String uuid);

  List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive);

  /**
   * Fired when the list of suspended user accounts has been updated.
   *
   * @param uuids UUIDs of suspended user accounts
   */
  void onSuspension(Set<String> uuids);
}

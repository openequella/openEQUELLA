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

package com.tle.core.usermanagement.standard.service;

import java.util.List;

import com.tle.beans.user.TLEGroup;
import com.tle.core.remoting.RemoteTLEGroupService;

public interface TLEGroupService extends RemoteTLEGroupService
{
	String add(TLEGroup group);

	TLEGroup createGroup(String groupID, String name);

	List<String> getUsersInGroup(String parentGroupID, boolean recurse);

	List<TLEGroup> getGroupsContainingUser(String userID, boolean recursive);

	void addUserToGroup(String groupUuid, String userUuid);

	void removeUserFromGroup(String groupUuid, String userUuid);

	void removeAllUsersFromGroup(String groupUuid);

	String prepareQuery(String searchString);
}
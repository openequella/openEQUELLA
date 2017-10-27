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

package com.tle.core.usermanagement.standard.dao;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.TLEGroup;
import com.tle.core.dao.AbstractTreeDao;

/**
 * @author Nicholas Read
 */
public interface TLEGroupDao extends AbstractTreeDao<TLEGroup>
{
	List<TLEGroup> listAllGroups();

	List<TLEGroup> getGroupsContainingUser(String userID);

	List<TLEGroup> searchGroups(String query, String parentId);

	List<String> getUsersInGroup(String parentGroupID, boolean recurse);

	TLEGroup findByUuid(String id);

	List<TLEGroup> getInformationForGroups(Collection<String> groups);

	boolean addUserToGroup(String groupUuid, String userUuid);

	boolean removeUserFromGroup(String groupUuid, String userUuid);
}

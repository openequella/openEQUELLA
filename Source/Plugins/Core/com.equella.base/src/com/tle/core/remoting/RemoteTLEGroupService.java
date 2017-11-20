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

package com.tle.core.remoting;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.GroupTreeNode;
import com.tle.beans.user.TLEGroup;

public interface RemoteTLEGroupService
{
	String add(String parentID, String name);

	TLEGroup get(String id);

	TLEGroup getByName(String name);

	String edit(final TLEGroup group);

	void delete(String groupID, boolean deleteChildren);

	List<TLEGroup> search(String query);

	List<TLEGroup> search(String query, String parentId);

	List<TLEGroup> search(String query, String userId, boolean allParents);

	GroupTreeNode searchTree(String query);

	List<TLEGroup> getInformationForGroups(Collection<String> groups);
}
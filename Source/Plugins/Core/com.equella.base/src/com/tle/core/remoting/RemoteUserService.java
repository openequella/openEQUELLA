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
import java.util.Map;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public interface RemoteUserService
{
	/**
	 * Resolve the list of userUniqueIDs into a list of UserBean objects. The
	 * returned list of users is in sync with the list of userUniqueIDs. If a
	 * given user ID cannot be resolved a null is placed in the returned list.
	 * 
	 * @param userUniqueIDs An collection of user unique IDs
	 * @return A list of UserBean objects
	 */
	Map<String, UserBean> getInformationForUsers(Collection<String> userUniqueIDs);

	/**
	 * @param userid
	 * @return null if no user is found
	 */
	UserBean getInformationForUser(String userid);

	List<RoleBean> getRolesForUser(String userid);

	List<String> getGroupIdsContainingUser(String userid);

	/**
	 * Get a list of groups that the user identified by userid belongs to. Will
	 * do a recursive search of groups.
	 * 
	 * @param userid
	 * @return
	 */
	List<GroupBean> getGroupsContainingUser(String userid);

	List<UserBean> getUsersInGroup(String groupId, boolean recursive);

	/**
	 * The user fields the query is matched with is User Management plugin
	 * dependent, but generally will attempt to match with username, first name,
	 * last name. Wildcards at the start and end of the query are implied. E.g.
	 * 'mit' will match 'smith'
	 * 
	 * @param query The username, first name or last name to search for
	 * @return
	 */
	List<UserBean> searchUsers(String query);

	/**
	 * Same as <code>searchUsers(query)</code>, but filters to the results to
	 * only users contained in the specified group, or subgroups if recursive.
	 * 
	 * @param query The username, first name or last name to search for
	 * @param parentGroupID The highest level group to search, or null if all
	 *            groups to be searched
	 * @param recurse Search subgroups
	 * @return
	 */
	List<UserBean> searchUsers(String query, String parentGroupID, boolean recurse);

	GroupBean getInformationForGroup(String uuid);

	Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs);

	List<GroupBean> searchGroups(String query);

	List<GroupBean> searchGroups(String query, String parentGroupID);

	RoleBean getInformationForRole(String uuid);

	Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs);

	List<RoleBean> searchRoles(String query);

	GroupBean getParentGroupForGroup(String groupID);

	void keepAlive();

	List<String> getTokenSecretIds();

	UserManagementSettings getPluginConfig(String settingsConfig);

	void setPluginConfig(UserManagementSettings config);

	void removeFromCache(String userid);
}
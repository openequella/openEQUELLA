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

package com.tle.common.scripting.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.GroupScriptType;
import com.tle.common.scripting.types.RoleScriptType;
import com.tle.common.scripting.types.UserScriptType;

/**
 * Referenced by the 'user' variable in script. Represents the currently logged
 * in user as well as provide an interface into the user service to retrieve
 * information about groups and roles. This is the SAME as
 * CommonUserScriptObject
 * 
 * @author aholland
 */
public interface UserScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "user"; //$NON-NLS-1$

	/**
	 * @return The given name of the logged in user.
	 */
	String getFirstName();

	/**
	 * @return The family name of the logged in user.
	 */
	String getLastName();

	/**
	 * @return The login name of the logged in user.
	 */
	String getUsername();

	/**
	 * @return The email address of the logged in user.
	 */
	String getEmail();

	/**
	 * @return The unique ID of the logged in user.
	 */
	String getID();

	/**
	 * Determines if the logged in user is a member of the group with the uuid
	 * of groupUniqueID
	 * 
	 * @param groupUniqueID The unique id (uuid) of the group
	 * @return Is member of the group?
	 */
	boolean isMemberOfGroup(String groupUniqueID);

	/**
	 * Determines if the logged in user has the role with the uuid of
	 * roleUniqueID
	 * 
	 * @param roleUniqueID The unique id (uuid) of the role to look for
	 * @return Has the role?
	 */
	boolean hasRole(String roleUniqueID);

	/**
	 * Opposite of hasRole. Developers: Why do we even have this?
	 * 
	 * @param roleUniqueID The unique id (uuid) of the role to look for
	 * @return Doesn't have the role?
	 */
	boolean doesntHaveRole(String roleUniqueID);

	/**
	 * Gets all the groups the logged in user belongs to
	 * 
	 * @return A list of GroupScriptType objects
	 */
	List<GroupScriptType> getGroups();

	/**
	 * @deprecated Use getGroups() instead
	 * @return An array of group uniqueIDs the logged in user belongs to.
	 */
	@Deprecated
	String[] getGroupIds();

	/**
	 * @deprecated Use getGroups() instead
	 * @return An array of group names the logged in user belongs to.
	 */
	@Deprecated
	String[] getGroupNames();

	/**
	 * Get a list of groups that the user identified by userUniqueID belongs to.
	 * Will do a recursive search of groups. This method has no relation to the
	 * logged in user.
	 * 
	 * @param userUniqueID The unique ID of the user.
	 * @return A list of GroupScriptType objects
	 */
	List<GroupScriptType> getGroupsContainingUser(String userUniqueID);

	/**
	 * The user fields the query is matched with is User Management plugin
	 * dependent, but generally will attempt to match with username, first name,
	 * last name. Wildcards at the start and end of the query are implied. E.g.
	 * 'mit' will match 'smith'. This method has no relation to the logged in
	 * user.
	 * 
	 * @param query The username, first name or last name to search for
	 * @return A list of UserScriptType objects
	 */
	List<UserScriptType> searchUsers(String query);

	/**
	 * Resolve the list of userUniqueIDs into a list of UserScriptType objects.
	 * The returned list of users may be shorter than userUniqueIDs if any user
	 * ID is not found, and the order of the result list bears no relation to
	 * the order of userUniqueIDs. This method has no relation to the logged in
	 * user.
	 * 
	 * @param userUniqueIDs An array of user unique IDs
	 * @return A list of UserScriptType objects
	 */
	List<UserScriptType> getInformationForUsers(String[] userUniqueIDs);

	/**
	 * Resolve the list of groupUniqueIDs into a list of GroupScriptType
	 * objects. The returned list of groups may be shorter than groupUniqueIDs
	 * if any group ID is not found, and the order of the result list bears no
	 * relation to the order of groupUniqueIDs. This method has no relation to
	 * the logged in user.
	 * 
	 * @param groupUniqueIDs An array of group unique IDs
	 * @return A list of GroupScriptType objects
	 */
	List<GroupScriptType> getInformationForGroups(String[] groupUniqueIDs);

	/**
	 * Same as searchUsers(String) but allows to filter the results by a group
	 * ID, and whether to search for subgroups of the specified group. Passing
	 * null for the groupUniqueID will be the equivalant of using
	 * searchUsers(String) Wildcards at the start and end of the query are
	 * implied. E.g. 'mit' will match 'smith'. This method has no relation to
	 * the logged in user.
	 * 
	 * @param query The username, first name or last name to search for
	 * @param groupUniqueID The group to search within
	 * @param recursive Search within subgroups of the group specified by
	 *            groupUniqueID
	 * @return A list of UserScriptType objects
	 */
	List<UserScriptType> searchUsersInGroups(String query, String groupUniqueID, boolean recursive);

	/**
	 * Find all groups with a name matching the query. Wildcards at the start
	 * and end of the query are implied. E.g. 'tude' will match 'Students'. This
	 * method has no relation to the logged in user.
	 * 
	 * @param query A group name to search for
	 * @return A list of GroupScriptType objects
	 */
	List<GroupScriptType> searchGroups(String query);

	/**
	 * Find all roles with a name matching the query. Wildcards at the start and
	 * end of the query are implied. E.g. 'strat' will match 'Administrators'.
	 * This method has no relation to the logged in user.
	 * 
	 * @param query A role name to search to for
	 * @return A list of RoleScriptType objects
	 */
	List<RoleScriptType> searchRoles(String query);
}

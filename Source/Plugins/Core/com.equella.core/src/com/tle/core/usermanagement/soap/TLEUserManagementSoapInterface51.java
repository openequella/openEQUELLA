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

package com.tle.core.usermanagement.soap;

public interface TLEUserManagementSoapInterface51 extends TLEUserManagementSoapInterface
{

	/**
	 * Finds all the internal EQUELLA users that match the searchString. If a
	 * groupUuid is provided the search is restricted to that group. If no
	 * groupUuid is provided the search will encompass all groups
	 * 
	 * @param groupUuid The unique ID of the group to search within
	 * @param searchString The string query to search for
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;users&gt;
	 *     &lt;user&gt;
	 *         &lt;uuid&gt;abc123&lt;/uuid&gt;
	 *         &lt;username&gt;username&lt;/username&gt;
	 *         &lt;firstname&gt;User&lt;/firstname&gt;
	 *         &lt;lastname&gt;Name&lt;/lastname&gt;
	 *         &lt;email&gt;username@example.com&lt;/email&gt;
	 *     &lt;/user&gt;
	 *     ...
	 * &lt;/users&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String searchUsersByGroup(String groupUuid, String searchString);

	/**
	 * Finds all the internal EQUELLA groups that match the search string
	 * 
	 * @param
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;groups&gt;
	 *     &lt;group&gt;
	 *         &lt;uuid&gt;abc123&lt;/uuid&gt;
	 *         &lt;name&gt;groupname&lt;/name&gt;
	 *     &lt;/group&gt;
	 *     ...
	 * &lt;/groups&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String searchGroups(String searchString);

	/**
	 * Finds all the internal EQUELLA groups that the user is a member of. This
	 * includes nested groups
	 * 
	 * @param userUuid
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;groups&gt;
	 *     &lt;group&gt;
	 *         &lt;uuid&gt;abc123&lt;/uuid&gt;
	 *         &lt;name&gt;groupname&lt;/name&gt;
	 *     &lt;/group&gt;
	 *     ...
	 * &lt;/groups&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String getGroupsByUser(String userUuid);

	/**
	 * Changes the name of an existing internal EQUELLA group to the name
	 * provided
	 * 
	 * @param groupUuid The UUID of the group to edit
	 * @param groupName The new name for the group
	 */
	void editGroup(String groupUuid, String groupName);

	/**
	 * Changes the parent of an existing internal EQUELLA group to the parent
	 * group specified
	 * 
	 * @param parentUuid The unique ID of the parentGroup
	 * @param groupUuid The unique ID of the group whos parent is being modified
	 */
	void setParentGroupForGroup(String parentGroupUuid, String groupUuid);
}

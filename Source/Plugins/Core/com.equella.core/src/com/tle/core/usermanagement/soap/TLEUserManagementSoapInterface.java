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

public interface TLEUserManagementSoapInterface
{
	/**
	 * Retrieves an internal EQUELLA user with the id of userId.
	 * 
	 * @param userId The unique ID of the user to retrieve.
	 * @return An XML user object representing the user's details.
	 *         <em>See the section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services} for the format of the User XML</em>
	 * @throws Exception Will throw a NotFoundException if the user cannot be
	 *             found.
	 */
	String getUser(String userId) throws Exception;

	/**
	 * Creates an internal EQUELLA user on the server. This does not assign the
	 * user to any groups, roles or permissions.
	 * 
	 * @param userId The unique ID of the new user.
	 * @param username The new user's username.
	 * @param password The new user's password.
	 * @param firstName The user's first name.
	 * @param lastName The user's last name.
	 * @param email The user's email address.
	 * @return The unique ID of the new user.
	 */
	String addUser(String userId, String username, String password, String firstName, String lastName, String email);

	/**
	 * Update the user on the server with the modifications made to the user.
	 * You may optionally specifiy a new password for the user.
	 * 
	 * @param userId The unique ID of the user to edit.
	 * @param username The user's new username.
	 * @param password The user's new password in plain text. May be null, in
	 *            which case the password is not changed.
	 * @param firstName The user's new first name.
	 * @param lastName The user's new last name.
	 * @param email The user's new email address.
	 * @return The unique ID of the user.
	 */
	String editUser(String userId, String username, String password, String firstName, String lastName, String email);

	/**
	 * Deletes the user from the EQUELLA server. Use with caution.
	 * 
	 * @param userId The unique ID of the user to delete.
	 */
	void deleteUser(String userId);

	/**
	 * Adds the user (external or internal) with the id of userId to the
	 * internal EQUELLA group with the id of groupId.
	 * 
	 * @param userId The unique ID of user to add.
	 * @param groupId The unique ID of the group to add to.
	 */
	void addUserToGroup(String userId, String groupId);

	/**
	 * Remove the user (external or internal) with the id of userId from the
	 * internal EQUELLA group with the id of groupId.
	 * 
	 * @param userId The unique ID of user to remove
	 * @param groupId The unique ID of the group to remove from.
	 */
	void removeUserFromGroup(String userId, String groupId);

	/**
	 * Remove the user (external or internal) with the id of userId from all
	 * internal EQUELLA groups. Use with caution.
	 * 
	 * @param userId The unqiue ID of the user to remove from all groups.
	 */
	void removeUserFromAllGroups(String userId);

	/**
	 * Is the user (external or internal) with id of userId <b>directly in</b>
	 * (that is, not in a subgroup of) the internal EQUELLA group with the id of
	 * groupId.
	 * 
	 * @param userId The unique ID of the user to check for.
	 * @param groupId The unique ID of the group to look in.
	 * @return true if user is in group.
	 */
	boolean isUserInGroup(String userId, String groupId);

	/**
	 * Determine if the internal EQUELLA user with the id of userId exists.
	 * 
	 * @param userId The unique ID of the user to find.
	 */
	boolean userExists(String userId);

	/**
	 * Determine if the internal EQUELLA user with the login name of username
	 * exists.
	 * 
	 * @param username The login name of the user to find.
	 */
	boolean userNameExists(String username);

	/**
	 * Determine if the internal EQUELLA group with the id of groupId exists.
	 * 
	 * @param groupId The unique ID of the group to find.
	 */
	boolean groupExists(String groupId);

	/**
	 * Find the id of the internal EQUELLA group with the name of groupName.
	 * 
	 * @param groupName The name of the group to find.
	 */
	String getGroupUuidForName(String groupName);

	/**
	 * Adds an internal EQUELLA group with the given groupId and groupName to
	 * the server.
	 * 
	 * @param groupId The unique ID of the new group.
	 * @param groupName The name of the new group.
	 */
	void addGroup(String groupId, String groupName);

	/**
	 * Deletes an internal EQUELLA group with the given groupId. Use with
	 * caution.
	 * 
	 * @param groupId The unique ID of the group to delete.
	 */
	void deleteGroup(String groupId);

	/**
	 * Removes all the users (internal and external) from the internal EQUELLA
	 * group with id of groupId. Use with caution.
	 * 
	 * @param groupId The unique ID of the group to remove all users from.
	 */
	void removeAllUsersFromGroup(String groupId);
}

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

/**
 * Version 2 of our general purpose Soap Interface. <br>
 * <strong>Important</strong> Sessions: Soap Sessions are now maintained by
 * using HTTP Cookies, so your client must support them in order to work with
 * SoapInterfaceV2. <br>
 * The session id you pass to each method will NOT be used to link to an
 * existing session, it will be used purely for diagnostic purposes. That is, if
 * the session id does not match the session retrieved by the server based on
 * the cookie the client sent (if indeed it did), then an exception will be
 * thrown.
 */
public interface SoapInterfaceV2
{
	/**
	 * Login with the given username and password.<br>
	 * Note that the response sent back by this method will include a new
	 * cookie. Your client MUST have cookies enabled. This method will throw an
	 * exception if Authentication fails.
	 * 
	 * @param username The username
	 * @param password The password
	 * @return A session id that can be used in subsequent method calls for
	 *         diagnostic purposes.<br>
	 *         <b>PLEASE NOTE:</b><br>
	 *         Your Soap Client must use HTTP Cookies in order to maintain a
	 *         session.
	 */
	String login(String username, String password);

	/**
	 * Login with the given token. The token format is described in the
	 * "LMS Integration Specification" document. Note that the response sent
	 * back by this method will include a new cookie. Your client MUST have
	 * cookies enabled.
	 * 
	 * @param token Token string
	 * @return A session id that can be used in subsequent method calls for
	 *         diagnostic purposes.<br>
	 *         <b>PLEASE NOTE:</b><br>
	 *         Your Soap Client must use HTTP Cookies in order to maintain a
	 *         session.
	 */
	String loginWithToken(String token);

	/**
	 * Logout the current user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 */
	void logout(String ssid);

	/**
	 * This is a light-weight method that can be invoked periodically to ensure
	 * that the current session does not timeout.
	 */
	void keepAlive();

	/**
	 * Retrieve a user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of the user to retrieve.
	 * @return An {@link User} object representing the user's details.
	 */
	User getUser(String ssid, String uuid);

	/**
	 * Add an internal user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of the new user.
	 * @param username The new user's username.
	 * @param password The new user's password.
	 * @param first The user's first name.
	 * @param last The user's last name.
	 * @param email The user's email address.
	 * @return The UUID of the new user.
	 */
	String addUser(String ssid, String uuid, String name, String password, String first, String last, String email);

	/**
	 * Edit an already existing internal user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of the user.
	 * @param username The user's new username.
	 * @param password The user's new password.
	 * @param first The user's new first name.
	 * @param last The user's new last name.
	 * @param email The user's new email address.
	 * @return The UUID of the user.
	 */
	String editUser(String ssid, String uuid, String name, String password, String first, String last, String email);

	/**
	 * Remove an internal user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of the user to remove.
	 */
	void removeUser(String ssid, String uuid);

	/**
	 * Add a user to an internal group.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of user to add.
	 * @param groupid The UUID of the group to add to.
	 */
	void addUserToGroup(String ssid, String uuid, String groupid);

	/**
	 * Remove a user from an internal group.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param uuid The UUID of the user to remove
	 * @param groupid The UUID of the group to remove from.
	 */
	void removeUserFromGroup(String ssid, String uuid, String groupid);

	/**
	 * Remove a user from any internal groups.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param userUuid The UUID of the user to remove from all groups.
	 */
	void removeUserFromAllGroups(String ssid, String userUuid);

	/**
	 * Check if a user is contained in a group.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param userUuid The UUID of the user to check for.
	 * @param groupUuid The UUID of the group to look in.
	 * @return true if user is in group.
	 */
	boolean isUserInGroup(String ssid, String userUuid, String groupUuid);

	/**
	 * Accept the moderation task for the given item.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 * @param taskId The UUID of the task to accept.
	 * @param unlock Whether or not to unlock the item after accepting.
	 * @return The UUID of the task that was accepted.
	 * @throws Exception
	 */
	String acceptTask(String ssid, String itemUuid, int itemVersion, String taskId, boolean unlock) throws Exception;

	/**
	 * Reject the moderation task for the given item.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 * @param taskId The UUID of the task to reject.
	 * @param rejectMessage The rejection reason.
	 * @param toStep The UUID of the task to reject back to.
	 * @param unlock Whether or not to unlock the item after accepting.
	 * @return The UUID of the task that was rejected.
	 * @throws Exception
	 */
	String rejectTask(String ssid, String itemUuid, int itemVersion, String taskId, String rejectMessage,
		String toStep, boolean unlock) throws Exception;

	/**
	 * Check to see if the local EQUELLA user exists with the given uuid * @param
	 * ssid The current session id (<strong>Important</strong>: please see
	 * documentation on {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param userUuid The UUID of the user.
	 */
	boolean userExists(String ssid, String userUuid);

	/**
	 * Check to see if the local EQUELLA user exists with the given login name * @param
	 * ssid The current session id (<strong>Important</strong>: please see
	 * documentation on {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param loginName The login name of the user.
	 */

	boolean userNameExists(String ssid, String loginName);

	/**
	 * Check to see if the local EQUELLA group exists with the given uuid * @param
	 * ssid The current session id (<strong>Important</strong>: please see
	 * documentation on {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param groupUuid The UUID of the group.
	 */

	boolean groupExists(String ssid, String groupUuid);

	/**
	 * Find the local EQUELLA group Uuid with the given groupName * @param ssid
	 * The current session id (<strong>Important</strong>: please see
	 * documentation on {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param groupName The name of the group.
	 */
	String getGroupUuidForName(String ssid, String groupName);

	/**
	 * Add a group with the given groupId and groupName to local EQUELLA users
	 * group * @param ssid The current session id (<strong>Important</strong>:
	 * please see documentation on {@link com.tle.core.remoting.SoapInterfaceV2
	 * sessions})
	 * 
	 * @param groupId The Uuid of the group.
	 * @param groupName The name of the group.
	 */
	void addGroup(String ssid, String groupId, String groupName);

	/**
	 * Remove a group with the given groupId from local EQUELLA users group * @param
	 * ssid The current session id (<strong>Important</strong>: please see
	 * documentation on {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param groupId The Uuid of the group.
	 */
	void removeGroup(String ssid, String groupId);

	/**
	 * Remove all the users of group with the given groupId from local EQUELLA
	 * users group * @param ssid The current session id
	 * (<strong>Important</strong>: please see documentation on
	 * {@link com.tle.core.remoting.SoapInterfaceV2 sessions})
	 * 
	 * @param groupId The Uuid of the group.
	 */
	void removeAllUsersFromGroup(String ssid, String groupId);

	/**
	 * A simplified User object used specifically for transport over SOAP.
	 */
	class User
	{
		private String uuid;
		private String firstName;
		private String lastName;
		private String email;
		private String username;

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public String getFirstName()
		{
			return firstName;
		}

		public void setFirstName(String firstName)
		{
			this.firstName = firstName;
		}

		public String getLastName()
		{
			return lastName;
		}

		public void setLastName(String lastName)
		{
			this.lastName = lastName;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}
	}

	/**
	 * A simplified ItemTask object used specifically for transport over SOAP.
	 */
	class ItemTaskSimple
	{
		private String itemUuid;
		private int itemVersion;
		private String taskUuid;

		public ItemTaskSimple(String itemUuid, int itemVersion, String taskUuid)
		{
			this.itemUuid = itemUuid;
			this.itemVersion = itemVersion;
			this.taskUuid = taskUuid;
		}

		public String getItemUuid()
		{
			return itemUuid;
		}

		public void setItemUuid(String itemUuid)
		{
			this.itemUuid = itemUuid;
		}

		public int getItemVersion()
		{
			return itemVersion;
		}

		public void setItemVersion(int itemVersion)
		{
			this.itemVersion = itemVersion;
		}

		public String getTaskUuid()
		{
			return taskUuid;
		}

		public void setTaskUuid(String taskUuid)
		{
			this.taskUuid = taskUuid;
		}
	}
}

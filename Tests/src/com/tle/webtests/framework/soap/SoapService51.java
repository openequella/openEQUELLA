package com.tle.webtests.framework.soap;

public interface SoapService51 extends SoapService50
{
	/*
	 * Hierarchy Soap Methods (These could go in a new interface)
	 */
	String getTopic(String topicUuid);

	String listTopics(String parentUuid);

	void deleteTopic(String topicUuid);

	String createTopic(String parentUuid, String topicXml, int index);

	void editTopic(String topicUuid, String topicXml);

	void moveTopic(String childId, String parentUuid, int index);

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

	String getTaskFilterCounts(boolean ignoreZero);

	String[] getTaskFilterNames();

	String getTaskList(String filterName, int start, int numResults) throws Exception;
}

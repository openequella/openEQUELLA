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

package com.tle.beans.usermanagement.standard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;

@SuppressWarnings("nls")
public class ReplicatedConfiguration extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	private static final String DIGEST = "SHA1";
	public static final String DIGEST_PLAINTEXT = "Plaintext";
	// The things one learns from Sonar's quibbles - the mutability of the
	// contents of a final array, so ...
	public static final List<String> DIGESTS = Collections
		.unmodifiableList(Arrays.asList(DIGEST_PLAINTEXT, "SHA1", "MD5"));

	private static final String AUTHENTICATE = "SELECT ID, Password, Suspended FROM [User] WHERE Username = ?";
	private static final String USER_INFO = "SELECT ID, Username, FirstName, LastName, EmailAddress FROM [User] WHERE ID IN ?";
	private static final String GROUP_INFO = "SELECT ID, Name FROM [Group] WHERE ID IN ?";
	private static final String SEARCH_USERS = "SELECT ID FROM [User] WHERE (Username LIKE ? OR FirstName LIKE ? OR LastName LIKE ?) ";
	private static final String SEARCH_GROUP = "SELECT ID FROM [Group] WHERE Name like ?";

	private static final String USER_ROLE = "SELECT ID, Name FROM [Roles] WHERE UserID = ?";
	private static final String SEARCH_ROLES = "SELECT ID, Name FROM [Roles] WHERE Name LIKE ?";
	private static final String ROLE_INFO = "SELECT ID, Name FROM [Roles] WHERE ID IN ?";

	private static final String USERS_IN_GROUP_RECURSIVE = "SELECT DISTINCT [Membership].UserID "
		+ "FROM [CachedGroupMembership] INNER JOIN [Membership] "
		+ "ON [Membership].GroupID = [CachedGroupMembership].ChildGroupID "
		+ "WHERE [CachedGroupMembership].AncestorGroupID = ?";

	private static final String SEARCH_GROUPS_IN_GROUP = "SELECT ID, Name FROM [Group] "
		+ "INNER JOIN [GroupMembership] ON [Group].ID = [GroupMembership].ParentGroupID "
		+ "WHERE [Group].Name LIKE ? AND [GroupMembership].ParentGroupID = ?";

	private static final String USERS_IN_GROUP = "SELECT [Membership].UserID "
		+ "FROM [Membership] WHERE [Membership].GroupID = ?";

	private static final String SEARCH_USERS_IN_GROUP_RECURSIVE = "SELECT DISTINCT [Membership].UserID "
		+ "FROM [CachedGroupMembership] INNER JOIN [Membership] "
		+ "ON [Membership].GroupID = [CachedGroupMembership].ChildGroupID "
		+ "INNER JOIN [User] ON [Membership].UserID = [User].ID "
		+ "WHERE [CachedGroupMembership].AncestorGroupID = ? AND ([User].Username LIKE ? OR [User].FirstName LIKE ? OR [User].LastName LIKE ?)";

	private static final String SEARCH_USERS_IN_GROUP = "SELECT [Membership].UserID "
		+ "FROM [Membership] INNER JOIN [User] ON [Membership].UserID = [User].ID "
		+ "WHERE [Membership].GroupID = ? AND ([User].Username LIKE ? OR [User].FirstName LIKE ? OR [User].LastName LIKE ?)";

	private static final String GROUPS_CONTAINING_USER = "SELECT DISTINCT [CachedGroupMembership].AncestorGroupID "
		+ "FROM [Membership] INNER JOIN [CachedGroupMembership] "
		+ "ON [Membership].GroupID = [CachedGroupMembership].ChildGroupID " + "WHERE [Membership].UserID = ?";

	private static final String PARENT_GROUP = "SELECT ID, Name FROM [Group] "
		+ "INNER JOIN [GroupMembership] ON [Group].ID = [GroupMembership].ParentGroupID "
		+ "WHERE [GroupMembership].ChildGroupID = ?";

	@Property(key = "replicated.authenticate")
	protected String authenticate;
	@Property(key = "replicated.userInfo")
	protected String userInfo;
	@Property(key = "replicated.groupInfo")
	protected String groupInfo;
	@Property(key = "replicated.searchUsers")
	protected String searchUsers;

	@Property(key = "replicated.searchGroups")
	protected String searchGroups;
	@Property(key = "replicated.searchGroupsInGroup")
	protected String searchGroupsInGroup;
	@Property(key = "replicated.searchGroupsInGroupRecursive")
	protected String searchGroupsInGroupRecursive;

	@Property(key = "replicated.usersInGroupRecursive")
	protected String usersInGroupRecursive;
	@Property(key = "replicated.usersInGroup")
	protected String usersInGroup;
	@Property(key = "replicated.searchUsersInGroupRecursive")
	protected String searchUsersInGroupRecursive;
	@Property(key = "replicated.searchUsersInGroup")
	protected String searchUsersInGroup;

	@Property(key = "replicated.groupsContainingUser")
	protected String groupsContainingUser;
	@Property(key = "replicated.userRoles")
	protected String userRoles;
	@Property(key = "replicated.searchRoles")
	protected String searchRoles;
	@Property(key = "replicated.roleInfo")
	protected String roleInfo;
	@Property(key = "replicated.parentGroup")
	protected String parentGroup;
	@Property(key = "replicated.digest")
	protected String digest;

	@Property(key = "replicated.jdbc.driver")
	private String driver;
	@Property(key = "replicated.jdbc.username")
	private String username;
	@Property(key = "replicated.jdbc.password")
	private String password;
	@Property(key = "replicated.jdbc.url")
	private String url;

	@Property(key = "replicated.enabled")
	private boolean enabled;

	public ReplicatedConfiguration()
	{
		setup();
	}

	protected void setup()
	{
		// nada
	}

	public static class EmptyConfiguration extends ReplicatedConfiguration
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void setup()
		{
			authenticate = Constants.BLANK;
			userInfo = Constants.BLANK;
			groupInfo = Constants.BLANK;
			searchUsers = Constants.BLANK;
			searchGroups = Constants.BLANK;
			searchGroupsInGroup = Constants.BLANK;
			usersInGroupRecursive = Constants.BLANK;
			usersInGroup = Constants.BLANK;
			searchUsersInGroupRecursive = Constants.BLANK;
			searchUsersInGroup = Constants.BLANK;
			groupsContainingUser = Constants.BLANK;
			userRoles = Constants.BLANK;
			searchRoles = Constants.BLANK;
			roleInfo = Constants.BLANK;
			parentGroup = Constants.BLANK;
			digest = DIGEST_PLAINTEXT;
		}
	}

	public static class TleConfiguration extends ReplicatedConfiguration
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void setup()
		{
			authenticate = AUTHENTICATE;
			userInfo = USER_INFO;
			groupInfo = GROUP_INFO;
			searchUsers = SEARCH_USERS;
			searchGroups = SEARCH_GROUP;
			searchGroupsInGroup = SEARCH_GROUPS_IN_GROUP;
			usersInGroupRecursive = USERS_IN_GROUP_RECURSIVE;
			usersInGroup = USERS_IN_GROUP;
			searchUsersInGroupRecursive = SEARCH_USERS_IN_GROUP_RECURSIVE;
			searchUsersInGroup = SEARCH_USERS_IN_GROUP;
			groupsContainingUser = GROUPS_CONTAINING_USER;
			userRoles = USER_ROLE;
			searchRoles = SEARCH_ROLES;
			roleInfo = ROLE_INFO;
			parentGroup = PARENT_GROUP;
			digest = DIGEST;
		}
	}

	public String getDigest()
	{
		return digest;
	}

	public void setDigest(String digest)
	{
		this.digest = digest;
	}

	public String getAuthenticate()
	{
		return authenticate;
	}

	public void setAuthenticate(String authenticate)
	{
		this.authenticate = authenticate;
	}

	public String getGroupInfo()
	{
		return groupInfo;
	}

	public void setGroupInfo(String groupInfo)
	{
		this.groupInfo = groupInfo;
	}

	public String getGroupsContainingUser()
	{
		return groupsContainingUser;
	}

	public void setGroupsContainingUser(String groupsContainingUser)
	{
		this.groupsContainingUser = groupsContainingUser;
	}

	public String getSearchGroups()
	{
		return searchGroups;
	}

	public void setSearchGroups(String searchGroups)
	{
		this.searchGroups = searchGroups;
	}

	public String getSearchGroupsInGroup()
	{
		return searchGroupsInGroup;
	}

	public void setSearchGroupsInGroup(String searchGroupsInGroup)
	{
		this.searchGroupsInGroup = searchGroupsInGroup;
	}

	public String getSearchGroupsInGroupRecursive()
	{
		return searchGroupsInGroupRecursive;
	}

	public void setSearchGroupsInGroupRecursive(String searchGroupsInGroupRecursive)
	{
		this.searchGroupsInGroupRecursive = searchGroupsInGroupRecursive;
	}

	public String getSearchUsers()
	{
		return searchUsers;
	}

	public void setSearchUsers(String searchUsers)
	{
		this.searchUsers = searchUsers;
	}

	public String getUserInfo()
	{
		return userInfo;
	}

	public void setUserInfo(String userInfo)
	{
		this.userInfo = userInfo;
	}

	public String getUsersInGroup()
	{
		return usersInGroup;
	}

	public void setUsersInGroup(String usersInGroup)
	{
		this.usersInGroup = usersInGroup;
	}

	public String getUsersInGroupRecursive()
	{
		return usersInGroupRecursive;
	}

	public void setUsersInGroupRecursive(String usersInGroupRecursive)
	{
		this.usersInGroupRecursive = usersInGroupRecursive;
	}

	public String getSearchUsersInGroupRecursive()
	{
		return searchUsersInGroupRecursive;
	}

	public void setSearchUsersInGroupRecursive(String searchUsersInGroupRecursive)
	{
		this.searchUsersInGroupRecursive = searchUsersInGroupRecursive;
	}

	public String getSearchUsersInGroup()
	{
		return searchUsersInGroup;
	}

	public void setSearchUsersInGroup(String searchUsersInGroup)
	{
		this.searchUsersInGroup = searchUsersInGroup;
	}

	public String getUserRoles()
	{
		return userRoles;
	}

	public void setUserRoles(String userRole)
	{
		this.userRoles = userRole;
	}

	public String getDriver()
	{
		return driver;
	}

	public void setDriver(String driver)
	{
		this.driver = driver;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getParentGroup()
	{
		return parentGroup;
	}

	public void setParentGroup(String parentGroup)
	{
		this.parentGroup = parentGroup;
	}

	public String getRoleInfo()
	{
		return roleInfo;
	}

	public void setRoleInfo(String roleInfo)
	{
		this.roleInfo = roleInfo;
	}

	public String getSearchRoles()
	{
		return searchRoles;
	}

	public void setSearchRoles(String searchRoles)
	{
		this.searchRoles = searchRoles;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}

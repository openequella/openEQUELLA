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

package com.tle.plugins.ump;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;

/**
 * Implements all of the methods and returns null (*not* an empty list) or other
 * meaningful return value indicating there are not results. Look at
 * {@link UserDirectoryUtils} for helper functions to fill in any gaps in your
 * implementations.
 * 
 * @author nick
 */
public abstract class AbstractUserDirectory implements UserDirectory
{
	private UserDirectoryChain head;

	@Override
	public final boolean initialise(UserDirectoryChain head, UserManagementSettings settings)
	{
		this.head = head;

		return initialise(settings);
	}

	/**
	 * @return true if the settings should be re-saved to the DB after
	 *         initialisation.
	 */
	protected abstract boolean initialise(UserManagementSettings settings);

	protected UserDirectoryChain getChain()
	{
		return head;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		// Nothing to do
	}

	@Override
	public ModifiableUserState authenticateUser(String username, String password)
	{
		return null;
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(String username, String privateData)
	{
		return null;
	}

	@Override
	public ModifiableUserState authenticateToken(String token)
	{
		return null;
	}

	@Override
	public ModifiableUserState authenticateRequest(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public void initGuestUserState(ModifiableUserState state)
	{
		// Nothing to do
	}

	@Override
	public void initSystemUserState(ModifiableUserState state)
	{
		// nothing by default
	}

	@Override
	public VerifyTokenResult verifyUserStateForToken(UserState userState, String token)
	{
		return VerifyTokenResult.PASS;
	}

	@Override
	public void keepAlive()
	{
		// Nothing to do
	}

	@Override
	public void logout(UserState state)
	{
		// Nothing to do
	}

	@Override
	public UserBean getInformationForUser(String userID)
	{
		return null;
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userIDs)
	{
		return null;
	}

	@Override
	public GroupBean getInformationForGroup(String groupID)
	{
		return null;
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs)
	{
		return null;
	}

	@Override
	public RoleBean getInformationForRole(String roleID)
	{
		return null;
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs)
	{
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<RoleBean>> getRolesForUser(String userID)
	{
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(String userID)
	{
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive)
	{
		// Default behaviour for existing wrappers.
		Check.checkNotEmpty(groupId);
		return searchUsers(null, groupId, recursive);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		// Default behaviour for existing wrappers.
		return searchUsers(query, null, false);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query, String parentGroupID, boolean recursive)
	{
		return null;
	}

	@Override
	public Collection<GroupBean> searchGroups(String query)
	{
		return null;
	}

	@Override
	public Collection<GroupBean> searchGroups(String query, String parentId)
	{
		return null;
	}

	@Override
	public GroupBean getParentGroupForGroup(String groupID)
	{
		return null;
	}

	@Override
	public Collection<RoleBean> searchRoles(String query)
	{
		return null;
	}

	@Override
	public void close() throws Exception
	{
		// Nothing to do
	}

	@Override
	public String getGeneratedToken(String secretId, String username)
	{
		return null;
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		return null;
	}
	
	@Override
	public void purgeFromCaches(String id)
	{
		// do nothing
	}
}

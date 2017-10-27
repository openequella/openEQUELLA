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

package com.tle.core.usermanagement.standard;

import static com.tle.plugins.ump.UserDirectoryUtils.makeCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.DefaultUserState;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.core.usermanagement.standard.ldap.LDAP;
import com.tle.core.usermanagement.standard.service.LDAPService;
import com.tle.exceptions.BadCredentialsException;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;

@Bind
@SuppressWarnings("nls")
public class LDAPUserPlugin extends AbstractUserDirectory
{
	private static final Logger LOGGER = Logger.getLogger(LDAPUserPlugin.class);

	private final Cache<String, List<String>> initGroupsCache = makeCache();

	@Inject
	private LDAPService ldapService;
	@Inject
	private EncryptionService encryptionService;

	private LDAP ldap;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		this.ldap = new LDAP((LDAPSettings) settings, encryptionService);
		return false;
	}

	@Override
	public ModifiableUserState authenticateUser(final String username, final String password)
	{
		try
		{
			String token = ldapService.searchAuthenticate(ldap, username, password);
			return resolveUserFromToken(token);
		}
		catch( BadCredentialsException bad )
		{
			LOGGER.warn(bad.getMessage());
			return null;
		}
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(final String username, String privateData)
	{
		return resolveUserFromToken(ldapService.getTokenFromUsername(ldap, username));
	}

	private ModifiableUserState resolveUserFromToken(String token)
	{
		if( token == null )
		{
			return null;
		}

		UserBean userb = ldapService.resolveUserFromToken(ldap, token);

		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(userb);

		String userId = userb.getUniqueID();
		List<String> cgs = initGroupsCache.getIfPresent(userId);
		if( cgs == null )
		{
			cgs = new ArrayList<String>();
			for( GroupBean gb : getGroupsContainingUser(userId).getSecond() )
			{
				cgs.add(gb.getUniqueID());
			}

			cgs = Collections.unmodifiableList(cgs);
			initGroupsCache.put(userId, cgs);
		}
		state.getUsersGroups().addAll(cgs);

		return state;
	}

	@Override
	public UserBean getInformationForUser(final String userID)
	{
		return ldapService.getUserBean(ldap, userID);
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userIds)
	{
		return UserDirectoryUtils.getMultipleUserInfosFromSingleInfos(this, userIds);
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(final String userID)
	{
		return new Pair<ChainResult, Collection<GroupBean>>(ChainResult.CONTINUE,
			ldapService.getGroupsContainingUser(ldap, userID));
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive)
	{
		return searchUsers("", groupId, recursive);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, ldapService.searchUsers(ldap, query));
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(final String query, final String parentGroupID,
		final boolean recursive)
	{
		if( Check.isEmpty(parentGroupID) )
		{
			return searchUsers(query);
		}

		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE,
			ldapService.getUsersInGroup(ldap, query, parentGroupID, recursive));
	}

	@Override
	public Collection<GroupBean> searchGroups(final String query)
	{
		return ldapService.searchGroups(ldap, query);
	}

	@Override
	public Collection<GroupBean> searchGroups(String query, String parentGroupId)
	{
		return ldapService.searchGroups(ldap, query, parentGroupId);
	}

	@Override
	public GroupBean getParentGroupForGroup(final String groupID)
	{
		return ldapService.getParentGroupForGroup(ldap, groupID);
	}

	@Override
	public GroupBean getInformationForGroup(final String groupID)
	{
		return ldapService.getGroupBean(ldap, groupID);
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds)
	{
		return UserDirectoryUtils.getMultipleGroupInfosFromSingleInfos(this, groupIds);
	}

	@Override
	public void purgeFromCaches(String id)
	{
		if( initGroupsCache.asMap().containsKey(id) )
		{
			initGroupsCache.invalidate(id);
		}
		else
		{
			// in case user was deleted from a group
			Iterator<Entry<String, List<String>>> it = initGroupsCache.asMap().entrySet().iterator();
			while( it.hasNext() && initGroupsCache.size() > 0 )
			{
				Entry<String, List<String>> cacheValues = it.next();
				for( String groupId : cacheValues.getValue() )
				{
					if( groupId.equals(id) )
					{
						initGroupsCache.invalidate(cacheValues.getKey());
					}
				}
			}
		}
	}
}

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

import static com.tle.plugins.ump.UserDirectoryUtils.makeCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.common.Constants;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.plugins.ump.UserDirectory.ChainResult;
import com.tle.plugins.ump.UserDirectory.VerifyTokenResult;

public class UserDirectoryChainImpl implements UserDirectoryChain
{
	private static final UserBean USER_NOT_FOUND = new EmptyUserBean();
	private static final RoleBean ROLE_NOT_FOUND = new EmptyRoleBean();
	private static final GroupBean GROUP_NOT_FOUND = new EmptyGroupBean();

	private final Cache<String, UserBean> userCache = makeCache();
	private final Cache<String, RoleBean> roleCache = makeCache();
	private final Cache<String, GroupBean> groupCache = makeCache();
	private final Cache<String, List<GroupBean>> groupsContainingUserCache = makeCache();
	private final Cache<String, List<UserBean>> searchUsersCache = makeCache();

	private List<UserDirectory> uds;

	public void setChain(List<UserDirectory> uds)
	{
		this.uds = uds;
	}

	@Override
	public void purgeFromCaches(String id)
	{
		userCache.invalidate(id);
		roleCache.invalidate(id);
		groupsContainingUserCache.invalidate(id);
		for( UserDirectory ud : uds )
		{
			ud.purgeFromCaches(id);
		}
		userCache.invalidate(id);
		roleCache.invalidate(id);
		groupCache.invalidate(id);
		groupsContainingUserCache.invalidate(id);
		searchUsersCache.invalidate(id);
	}

	@Override
	public void purgeGroupFromCaches(String groupId)
	{
		groupCache.invalidate(groupId);
		// user is deleted from a group
		Iterator<Entry<String, List<GroupBean>>> it = groupsContainingUserCache.asMap().entrySet().iterator();
		while( it.hasNext() && groupsContainingUserCache.size() > 0 )
		{
			Entry<String, List<GroupBean>> cacheValues = it.next();
			for( GroupBean group : cacheValues.getValue() )
			{
				if( group.getUniqueID().equals(groupId) )
				{
					groupsContainingUserCache.invalidate(cacheValues.getKey());
				}
			}

		}
		for( UserDirectory ud : uds )
		{
			ud.purgeFromCaches(groupId);
		}
	}

	@Override
	public ModifiableUserState authenticateRequest(HttpServletRequest request)
	{
		for( UserDirectory ud : uds )
		{
			ModifiableUserState mus = ud.authenticateRequest(request);
			if( mus != null )
			{
				return mus;
			}
		}
		return null;
	}

	@Override
	public ModifiableUserState authenticateToken(String token)
	{
		for( UserDirectory ud : uds )
		{
			ModifiableUserState mus = ud.authenticateToken(token);
			if( mus != null )
			{
				return mus;
			}
		}
		return null;
	}

	@Override
	public ModifiableUserState authenticateUser(String username, String password)
	{
		for( UserDirectory ud : uds )
		{
			ModifiableUserState mus = ud.authenticateUser(username, password);
			if( mus != null )
			{
				return mus;
			}
		}
		return null;
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(String username, String privateData)
	{
		for( UserDirectory ud : uds )
		{
			ModifiableUserState mus = ud.authenticateUserFromUsername(username, privateData);
			if( mus != null )
			{
				return mus;
			}
		}
		return null;
	}

	@Override
	public void close() throws Exception
	{
		for( UserDirectory ud : uds )
		{
			ud.close();
		}
	}

	@Override
	public String getGeneratedToken(String secretId, String username)
	{
		for( UserDirectory ud : uds )
		{
			String token = ud.getGeneratedToken(secretId, username);
			if( token != null )
			{
				return token;
			}
		}
		return null;
	}

	@Override
	public List<GroupBean> getGroupsContainingUser(String userId)
	{
		if( Check.isEmpty(userId) )
		{
			return Collections.emptyList();
		}

		List<GroupBean> rv = groupsContainingUserCache.getIfPresent(userId);
		if( rv != null )
		{
			return rv;
		}

		for( UserDirectory ud : uds )
		{
			Pair<ChainResult, Collection<GroupBean>> gcu = ud.getGroupsContainingUser(userId);
			if( gcu != null )
			{
				rv = accumulate(rv, gcu.getSecond());
				if( gcu.getFirst() == ChainResult.STOP )
				{
					break;
				}
			}
		}

		rv = emptyOrUnmodifiable(rv);
		groupsContainingUserCache.put(userId, rv);
		return rv;
	}

	@Override
	public List<UserBean> getUsersInGroup(String groupId, boolean recursive)
	{
		Check.checkNotEmpty(groupId);

		List<UserBean> rv = null;
		for( UserDirectory ud : uds )
		{
			Pair<ChainResult, Collection<UserBean>> ufg = ud.getUsersForGroup(groupId, recursive);
			if( ufg != null )
			{
				rv = accumulate(rv, ufg.getSecond());
				if( ufg.getFirst() == ChainResult.STOP )
				{
					break;
				}
			}
		}
		return nullToEmpty(rv);
	}

	@Override
	public GroupBean getInformationForGroup(String groupId)
	{
		if( groupId == null )
		{
			return null;
		}

		GroupBean cgb = groupCache.getIfPresent(groupId);
		if( cgb != null )
		{
			return cgb.equals(GROUP_NOT_FOUND) ? null : cgb;
		}

		for( UserDirectory ud : uds )
		{
			GroupBean gb = ud.getInformationForGroup(groupId);
			if( gb != null )
			{
				groupCache.put(groupId, gb);
				return gb;
			}
		}

		groupCache.put(groupId, GROUP_NOT_FOUND);
		return null;
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds)
	{
		if( Check.isEmpty(groupIds) )
		{
			return Collections.emptyMap();
		}

		Set<String> gids = new HashSet<String>(groupIds);
		gids.remove(null);
		gids.remove(Constants.BLANK);

		Map<String, GroupBean> rv = Maps.newHashMapWithExpectedSize(gids.size());

		// Populate from the cache first
		for( String groupId : gids )
		{
			GroupBean cgb = groupCache.getIfPresent(groupId);
			if( cgb != null && !cgb.equals(GROUP_NOT_FOUND) )
			{
				rv.put(groupId, cgb);
			}
		}

		for( UserDirectory ud : uds )
		{
			Map<String, GroupBean> found = ud.getInformationForGroups(gids);
			if( !Check.isEmpty(found) )
			{
				gids.removeAll(found.keySet());

				rv.putAll(found);
				groupCache.putAll(found);
			}

			if( gids.isEmpty() )
			{
				break;
			}
		}

		// Make sure we mark invalid/unfound ids in the cache
		for( String groupId : gids )
		{
			groupCache.put(groupId, GROUP_NOT_FOUND);
		}

		return rv;
	}

	@Override
	public RoleBean getInformationForRole(String roleId)
	{
		if( roleId == null )
		{
			return null;
		}

		RoleBean cub = roleCache.getIfPresent(roleId);
		if( cub != null )
		{
			return cub.equals(ROLE_NOT_FOUND) ? null : cub;
		}

		for( UserDirectory ud : uds )
		{
			RoleBean rb = ud.getInformationForRole(roleId);
			if( rb != null )
			{
				roleCache.put(roleId, rb);
				return rb;
			}
		}

		roleCache.put(roleId, ROLE_NOT_FOUND);
		return null;
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds)
	{
		if( Check.isEmpty(roleIds) )
		{
			return Collections.emptyMap();
		}

		Set<String> rids = new HashSet<String>(roleIds);
		rids.remove(null);
		rids.remove(Constants.BLANK);

		Map<String, RoleBean> rv = Maps.newHashMapWithExpectedSize(rids.size());

		// Populate from the cache first
		for( String roleId : rids )
		{
			RoleBean crb = roleCache.getIfPresent(roleId);
			if( crb != null && !crb.equals(ROLE_NOT_FOUND) )
			{
				rv.put(roleId, crb);
			}
		}

		for( UserDirectory ud : uds )
		{
			Map<String, RoleBean> found = ud.getInformationForRoles(rids);
			if( !Check.isEmpty(found) )
			{
				rids.removeAll(found.keySet());

				rv.putAll(found);
				roleCache.putAll(found);
			}

			if( rids.isEmpty() )
			{
				break;
			}
		}

		// Make sure we mark invalid/unfound ids in the cache
		for( String roleId : rids )
		{
			roleCache.put(roleId, ROLE_NOT_FOUND);
		}

		return rv;
	}

	@Override
	public UserBean getInformationForUser(String userId)
	{
		if( userId == null )
		{
			return null;
		}

		UserBean cub = userCache.getIfPresent(userId);
		if( cub != null )
		{
			return cub.equals(USER_NOT_FOUND) ? null : cub;
		}

		for( UserDirectory ud : uds )
		{
			UserBean ub = ud.getInformationForUser(userId);
			if( ub != null )
			{
				userCache.put(userId, ub);
				return ub;
			}
		}

		userCache.put(userId, USER_NOT_FOUND);
		return null;
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userIds)
	{
		if( Check.isEmpty(userIds) )
		{
			return Collections.emptyMap();
		}

		Set<String> uids = new HashSet<String>(userIds);
		uids.remove(null);
		uids.remove(Constants.BLANK);

		Map<String, UserBean> rv = Maps.newHashMapWithExpectedSize(uids.size());

		// Populate from the cache first
		for( String userId : uids )
		{
			UserBean cub = userCache.getIfPresent(userId);
			if( cub != null && !cub.equals(USER_NOT_FOUND) )
			{
				rv.put(userId, cub);
			}
		}

		// Maybe we got it all from the cache!
		uids.removeAll(rv.keySet());
		if( uids.isEmpty() )
		{
			return rv;
		}

		// Find any un-cached information
		for( UserDirectory ud : uds )
		{
			Map<String, UserBean> found = ud.getInformationForUsers(uids);
			if( !Check.isEmpty(found) )
			{
				uids.removeAll(found.keySet());

				rv.putAll(found);
				userCache.putAll(found);
			}

			if( uids.isEmpty() )
			{
				break;
			}
		}

		// Make sure we mark invalid/unfound ids in the cache
		for( String userId : uids )
		{
			userCache.put(userId, USER_NOT_FOUND);
		}

		return rv;
	}

	@Override
	public GroupBean getParentGroupForGroup(String groupId)
	{
		for( UserDirectory ud : uds )
		{
			GroupBean gb = ud.getParentGroupForGroup(groupId);
			if( gb != null )
			{
				return gb;
			}
		}
		return null;
	}

	@Override
	public List<RoleBean> getRolesForUser(String userId)
	{
		List<RoleBean> rv = null;
		for( UserDirectory ud : uds )
		{
			Pair<ChainResult, Collection<RoleBean>> rfu = ud.getRolesForUser(userId);
			if( rfu != null )
			{
				rv = accumulate(rv, rfu.getSecond());
				if( rfu.getFirst() == ChainResult.STOP )
				{
					break;
				}
			}
		}
		return nullToEmpty(rv);
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		List<String> rv = null;
		for( UserDirectory ud : uds )
		{
			List<String> tokens = ud.getTokenSecretIds();
			rv = accumulate(rv, tokens);
		}
		return nullToEmpty(rv);
	}

	@Override
	public void initGuestUserState(ModifiableUserState state)
	{
		// Let the more authoritative user directories supply details first by
		// doing this in reverse.
		for( UserDirectory ud : Lists.reverse(uds) )
		{
			ud.initGuestUserState(state);
		}
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		// Let the more authoritative user directories supply details first by
		// doing this in reverse.
		for( UserDirectory ud : Lists.reverse(uds) )
		{
			ud.initUserState(state);
		}
	}

	@Override
	public void initSystemUserState(ModifiableUserState state)
	{
		// Let the more authoritative user directories supply details first by
		// doing this in reverse.
		for( UserDirectory ud : Lists.reverse(uds) )
		{
			ud.initSystemUserState(state);
		}
	}

	@Override
	public void keepAlive()
	{
		for( UserDirectory ud : uds )
		{
			ud.keepAlive();
		}
	}

	@Override
	public void logout(UserState state)
	{
		for( UserDirectory ud : uds )
		{
			ud.logout(state);
		}
	}

	@Override
	public List<GroupBean> searchGroups(String query)
	{
		List<GroupBean> rv = null;
		for( UserDirectory ud : uds )
		{
			rv = accumulate(rv, ud.searchGroups(query));
		}
		return nullToEmpty(rv);
	}

	@Override
	public List<GroupBean> searchGroups(String query, String parentId)
	{
		List<GroupBean> rv = null;
		for( UserDirectory ud : uds )
		{
			rv = accumulate(rv, ud.searchGroups(query, parentId));
		}
		return nullToEmpty(rv);
	}

	@Override
	public List<RoleBean> searchRoles(String query)
	{
		List<RoleBean> rv = null;
		for( UserDirectory ud : uds )
		{
			rv = accumulate(rv, ud.searchRoles(query));
		}
		return nullToEmpty(rv);
	}

	@Override
	public List<UserBean> searchUsers(final String query)
	{
		// The more complex version of this method already contains the logic
		// for ignoring the empty parent group ID and calling this version on
		// the chain.
		return searchUsers(query, null, false);
	}

	@Override
	public List<UserBean> searchUsers(String query, String parentGroupId, boolean recursive)
	{
		boolean noGroupId = Check.isEmpty(parentGroupId);
		final String cacheKey = noGroupId ? query : query + parentGroupId + recursive;

		List<UserBean> rv = searchUsersCache.getIfPresent(cacheKey);
		if( rv != null )
		{
			return rv;
		}

		for( UserDirectory ud : uds )
		{
			Pair<ChainResult, Collection<UserBean>> results = noGroupId ? ud.searchUsers(query) : ud.searchUsers(query,
				parentGroupId, recursive);
			if( results != null )
			{
				rv = accumulate(rv, results.getSecond());
				if( results.getFirst() == ChainResult.STOP )
				{
					break;
				}
			}
		}

		rv = emptyOrUnmodifiable(rv);
		searchUsersCache.put(cacheKey, rv);
		return rv;
	}

	@Override
	public boolean verifyUserStateForToken(UserState userState, String token)
	{
		for( UserDirectory ud : uds )
		{
			VerifyTokenResult vtr = ud.verifyUserStateForToken(userState, token);
			if( vtr != VerifyTokenResult.PASS )
			{
				return vtr == VerifyTokenResult.VALID;
			}
		}
		return false;
	}

	private static <U> List<U> accumulate(List<U> rv, Collection<U> newValues)
	{
		if( !Check.isEmpty(newValues) )
		{
			if( rv == null )
			{
				rv = Lists.newArrayListWithCapacity(newValues.size());
			}
			rv.addAll(newValues);
		}
		return rv;
	}

	private static <U> List<U> nullToEmpty(List<U> rv)
	{
		if( rv == null )
		{
			rv = Collections.emptyList();
		}
		return rv;
	}

	private static <U> List<U> emptyOrUnmodifiable(List<U> rv)
	{
		if( rv == null )
		{
			return Collections.emptyList();
		}
		else
		{
			return Collections.unmodifiableList(rv);
		}
	}

	private static final class EmptyUserBean implements UserBean
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getUniqueID()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getUsername()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getFirstName()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getLastName()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getEmailAddress()
		{
			throw new IllegalStateException();
		}
	}

	private static final class EmptyGroupBean implements GroupBean
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getUniqueID()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getName()
		{
			throw new IllegalStateException();
		}
	}

	private static final class EmptyRoleBean implements RoleBean
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getUniqueID()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getName()
		{
			throw new IllegalStateException();
		}
	}

	@Override
	public void clearUserSearchCache()
	{
		searchUsersCache.invalidateAll();
	}
}

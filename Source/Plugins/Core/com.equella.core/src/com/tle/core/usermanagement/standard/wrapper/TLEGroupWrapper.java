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

package com.tle.core.usermanagement.standard.wrapper;

import static com.tle.plugins.ump.UserDirectoryUtils.makeCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.DefaultGroupBean;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;

@Bind
public class TLEGroupWrapper extends AbstractUserDirectory
{
	private final Cache<String, List<String>> initGroupsCache = makeCache();

	@Inject
	private TLEGroupService groupService;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		return false;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		final String userId = state.getUserBean().getUniqueID();

		List<String> cgs = initGroupsCache.getIfPresent(userId);
		if( cgs == null )
		{
			cgs = new ArrayList<String>();
			for( TLEGroup group : groupService.getGroupsContainingUser(userId, true) )
			{
				cgs.add(group.getUuid());
			}
			cgs = Collections.unmodifiableList(cgs);
			initGroupsCache.put(userId, cgs);
		}
		state.getUsersGroups().addAll(cgs);
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(final String userID)
	{
		return new Pair<ChainResult, Collection<GroupBean>>(ChainResult.CONTINUE,
			convert(groupService.getGroupsContainingUser(userID, true)));
	}

	@Override
	public GroupBean getParentGroupForGroup(final String groupID)
	{
		TLEGroup group = groupService.get(groupID);
		if( group != null )
		{
			return convert(group.getParent());
		}
		return null;
	}

	@Override
	public GroupBean getInformationForGroup(final String groupID)
	{
		return convert(groupService.get(groupID));
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(final Collection<String> groupIds)
	{
		Map<String, GroupBean> rv = Maps.newHashMapWithExpectedSize(groupIds.size());
		for( TLEGroup tleg : groupService.getInformationForGroups(groupIds) )
		{
			GroupBean gb = convert(tleg);
			rv.put(gb.getUniqueID(), gb);
		}
		return rv;
	}

	@Override
	public Collection<GroupBean> searchGroups(final String query)
	{
		return convert(groupService.search(query));
	}

	@Override
	public Collection<GroupBean> searchGroups(String query, String parentGroupId)
	{
		return convert(groupService.search(query, parentGroupId));
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive)
	{
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getChain().getInformationForUsers(
			groupService.getUsersInGroup(groupId, recursive)).values());
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query, String parentGroupID, boolean recursive)
	{
		// We only care if the search is filtering by a group.
		if( Check.isEmpty(parentGroupID) )
		{
			return null;
		}

		// And we only care if the group ID is for one of ours.
		TLEGroup group = groupService.get(parentGroupID);
		if( group == null )
		{
			return null;
		}

		final List<String> usersInGroup = groupService.getUsersInGroup(parentGroupID, recursive);

		// If there are no users in the group, then return nothing.
		if( usersInGroup.isEmpty() )
		{
			return null;
		}

		// If there's no user query to filter on, return what we have.
		if( !UserDirectoryUtils.searchQueryContainsNonWildcards(query) )
		{
			return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getChain().getInformationForUsers(
				usersInGroup).values());
		}

		// Get all the results for a non grouped search from everyone on the
		// chain, then filter out things. Make sure any result from here on
		// include ChainResult.STOP!

		Collection<UserBean> users = getChain().searchUsers(query);
		if( !users.isEmpty() )
		{
			// Return a view rather than a new collection with elements removed.
			users = Collections2.filter(users, new Predicate<UserBean>()
			{
				private final Set<String> inGroup = new HashSet<String>(usersInGroup);

				@Override
				public boolean apply(UserBean user)
				{
					return inGroup.contains(user.getUniqueID());
				}
			});
		}
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.STOP, users);
	}

	private static GroupBean convert(TLEGroup group)
	{
		if( group == null )
		{
			return null;
		}
		else
		{
			return new DefaultGroupBean(group.getUuid(), group.getName());
		}
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

	private static Collection<GroupBean> convert(Collection<TLEGroup> gs)
	{
		Collection<GroupBean> rv = new ArrayList<GroupBean>(gs.size());
		for( TLEGroup g : gs )
		{
			rv.add(convert(g));
		}
		return rv;
	}

}

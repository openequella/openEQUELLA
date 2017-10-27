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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;

public final class UserDirectoryUtils
{
	/**
	 * User, group and role information is already cached in the UDC, but there
	 * may be a need for caching specific information in a UD, for example,
	 * TLEGroupWrapper caches group UUIDs for a user during initState to speed
	 * up repeated logins.
	 */
	public static <T> Cache<String, T> makeCache()
	{
		// Maximum size to so that we don't fill up the memory. Entries expire
		// 10 minutes after being *added* (not accessed) so that the details
		// can't be stale for too long. Soft values allow for GC to throw away
		// values if it needs to in LRU order, which is really nice and
		// automatic for us!
		return CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(5000).softValues().build();
	}

	public static UserBean getSingleUserInfoFromMultipleInfo(UserDirectory ud, String userId)
	{
		Map<String, UserBean> m = ud.getInformationForUsers(Collections.singleton(userId));
		if( Check.isEmpty(m) )
		{
			return null;
		}
		return m.get(userId);
	}

	public static Map<String, UserBean> getMultipleUserInfosFromSingleInfos(UserDirectory ud, Collection<String> userIds)
	{
		Map<String, UserBean> rv = null;
		for( String userId : userIds )
		{
			UserBean ub = ud.getInformationForUser(userId);
			if( ub != null )
			{
				if( rv == null )
				{
					rv = Maps.newHashMapWithExpectedSize(userIds.size());
				}
				rv.put(userId, ub);
			}
		}
		return rv;
	}

	public static GroupBean getSingleGroupInfoFromMultipleInfo(UserDirectory ud, String groupId)
	{
		Map<String, GroupBean> m = ud.getInformationForGroups(Collections.singleton(groupId));
		if( Check.isEmpty(m) )
		{
			return null;
		}
		return m.get(groupId);
	}

	public static Map<String, GroupBean> getMultipleGroupInfosFromSingleInfos(UserDirectory ud,
		Collection<String> groupIds)
	{
		Map<String, GroupBean> rv = null;
		for( String groupID : groupIds )
		{
			GroupBean gb = ud.getInformationForGroup(groupID);
			if( gb != null )
			{
				if( rv == null )
				{
					rv = Maps.newHashMapWithExpectedSize(groupIds.size());
				}
				rv.put(groupID, gb);
			}
		}
		return rv;
	}

	public static RoleBean getSingleRoleInfoFromMultipleInfo(UserDirectory ud, String roleId)
	{
		Map<String, RoleBean> m = ud.getInformationForRoles(Collections.singleton(roleId));
		if( Check.isEmpty(m) )
		{
			return null;
		}
		return m.get(roleId);
	}

	public static Map<String, RoleBean> getMultipleRoleInfosFromSingleInfos(UserDirectory ud, Collection<String> roleIds)
	{
		Map<String, RoleBean> rv = null;
		for( String roleID : roleIds )
		{
			RoleBean gb = ud.getInformationForRole(roleID);
			if( gb != null )
			{
				if( rv == null )
				{
					rv = Maps.newHashMapWithExpectedSize(roleIds.size());
				}
				rv.put(roleID, gb);
			}
		}
		return rv;
	}

	public static boolean searchQueryContainsNonWildcards(String query)
	{
		if( query != null )
		{
			for( int i = 0; i < query.length(); i++ )
			{
				if( Character.isLetterOrDigit(query.codePointAt(i)) )
				{
					return true;
				}
			}
		}
		return false;
	}

	private UserDirectoryUtils()
	{
		super();
	}
}

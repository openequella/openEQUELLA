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

package com.tle.web.scripting.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.common.scripting.objects.UserScriptObject;
import com.tle.common.scripting.types.GroupScriptType;
import com.tle.common.scripting.types.RoleScriptType;
import com.tle.common.scripting.types.UserScriptType;
import com.tle.common.scripting.types.impl.GroupScriptTypeImpl;
import com.tle.common.scripting.types.impl.RoleScriptTypeImpl;
import com.tle.common.scripting.types.impl.UserScriptTypeImpl;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.UserService;

public class UserScriptWrapper extends AbstractScriptWrapper implements UserScriptObject
{
	private static final long serialVersionUID = 1L;

	private final UserBean userBean;
	private final UserService userService;

	private String[] groupIds;
	private List<GroupBean> groups;
	private Set<String> roles;

	public UserScriptWrapper(UserService userService)
	{
		this.userService = userService;
		userBean = CurrentUser.getDetails();
		UserState userState = CurrentUser.getUserState();
		Set<String> groupSet = userState.getUsersGroups();
		groupIds = groupSet.toArray(new String[groupSet.size()]);
		roles = userState.getUsersRoles();
	}

	@Override
	public String getFirstName()
	{
		return userBean.getFirstName();
	}

	@Override
	public String getLastName()
	{
		return userBean.getLastName();
	}

	@Override
	public String getUsername()
	{
		return userBean.getUsername();
	}

	@Override
	public String getEmail()
	{
		return userBean.getEmailAddress();
	}

	@Override
	public String getID()
	{
		return userBean.getUniqueID();
	}

	@Override
	public boolean isMemberOfGroup(String groupUniqueID)
	{
		for( GroupBean group : getGroupBeans() )
		{
			if( group.getUniqueID().equals(groupUniqueID) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getGroupNames()
	{
		return transformGroupsToStrings(new Function<GroupBean, String>()
		{
			@Override
			public String apply(GroupBean gb)
			{
				return gb.getName();
			}
		});
	}

	@Override
	public String[] getGroupIds()
	{
		if( groupIds != null )
		{
			return groupIds;
		}
		return transformGroupsToStrings(new Function<GroupBean, String>()
		{
			@Override
			public String apply(GroupBean gb)
			{
				return gb.getUniqueID();
			}
		});
	}

	@Override
	public List<GroupScriptType> getGroups()
	{
		return transformGroups(getGroupBeans());
	}

	private String[] transformGroupsToStrings(Function<GroupBean, String> transformer)
	{
		List<GroupBean> grps = getGroupBeans();
		return Lists.transform(grps, transformer).toArray(new String[grps.size()]);
	}

	private synchronized List<GroupBean> getGroupBeans()
	{
		if( groups == null )
		{
			groups = userService.getGroupsContainingUser(userBean.getUniqueID());
		}
		return groups;
	}

	@Override
	public boolean hasRole(String roleUniqueID)
	{
		if( roles == null )
		{
			List<RoleBean> roles2 = userService.getRolesForUser(userBean.getUniqueID());
			roles = new HashSet<String>();
			for( RoleBean bean : roles2 )
			{
				roles.add(bean.getUniqueID());
			}
		}

		return roles.contains(roleUniqueID);
	}

	@Override
	public boolean doesntHaveRole(String name)
	{
		return !hasRole(name);
	}

	@Override
	public List<GroupScriptType> getGroupsContainingUser(String userid)
	{
		return transformGroups(userService.getGroupsContainingUser(userid));
	}

	@Override
	public List<UserScriptType> searchUsers(String query)
	{
		return transformUsers(userService.searchUsers(query));
	}

	@Override
	public List<UserScriptType> getInformationForUsers(String[] userUniqueIDs)
	{
		return transformUsers(userService.getInformationForUsers(Arrays.asList(userUniqueIDs)).values());
	}

	@Override
	public List<GroupScriptType> getInformationForGroups(String[] groupUniqueIDs)
	{
		return transformGroups(userService.getInformationForGroups(Arrays.asList(groupUniqueIDs)).values());
	}

	@Override
	public List<UserScriptType> searchUsersInGroups(String query, String groupid, boolean recursive)
	{
		return transformUsers(userService.searchUsers(query, groupid, recursive));
	}

	@Override
	public List<GroupScriptType> searchGroups(String query)
	{
		return transformGroups(userService.searchGroups(query));
	}

	@Override
	public List<RoleScriptType> searchRoles(String query)
	{
		return Lists
			.newArrayList(Lists.transform(userService.searchRoles(query), new Function<RoleBean, RoleScriptType>()
			{
				@Override
				public RoleScriptType apply(RoleBean role)
				{
					return new RoleScriptTypeImpl(role);
				}
			}));
	}

	private List<GroupScriptType> transformGroups(Collection<GroupBean> grps)
	{
		return Lists.newArrayList(Collections2.transform(grps, new Function<GroupBean, GroupScriptType>()
		{
			@Override
			public GroupScriptType apply(GroupBean grp)
			{
				return new GroupScriptTypeImpl(grp);
			}
		}));
	}

	private List<UserScriptType> transformUsers(Collection<UserBean> users)
	{
		return Lists.newArrayList(Collections2.transform(users, new Function<UserBean, UserScriptType>()
		{
			@Override
			public UserScriptType apply(UserBean usr)
			{
				return new UserScriptTypeImpl(usr);
			}
		}));
	}
}

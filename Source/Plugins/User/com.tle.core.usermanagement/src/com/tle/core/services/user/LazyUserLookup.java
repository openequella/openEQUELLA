package com.tle.core.services.user;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.common.usermanagement.user.valuebean.UserBean;

public class LazyUserLookup
{
	private Set<String> usersToLookup = Sets.newHashSet();
	private Map<String, UserBean> lookedUpUsers = Maps.newHashMap();
	private UserService userService;

	public LazyUserLookup(UserService userService)
	{
		this.userService = userService;
	}

	public void addUser(String userUuid)
	{
		usersToLookup.add(userUuid);
	}

	public UserBean get(String userUuid)
	{
		if( usersToLookup.contains(userUuid) )
		{
			lookedUpUsers.putAll(userService.getInformationForUsers(usersToLookup));
			usersToLookup.clear();
		}
		return lookedUpUsers.get(userUuid);
	}
}

package com.tle.client.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.core.remoting.RemoteUserService;

public class CachingUserServiceImpl implements RemoteUserService
{
	private static final Cache<String, UserBean> USER_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.HOURS).build();
	private static final Cache<String, GroupBean> GROUP_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.HOURS).build();
	private static final Cache<String, RoleBean> ROLE_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.HOURS).build();

	private final RemoteUserService remoteUserService;

	public CachingUserServiceImpl(RemoteUserService remoteUserService)
	{
		this.remoteUserService = remoteUserService;
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userUniqueIDs)
	{
		return remoteUserService.getInformationForUsers(userUniqueIDs);
	}

	@Override
	public UserBean getInformationForUser(String userid)
	{
		UserBean userInfo = USER_CACHE.getIfPresent(userid);
		if( userInfo == null )
		{
			userInfo = remoteUserService.getInformationForUser(userid);
			if( userInfo != null )
			{
				USER_CACHE.put(userid, userInfo);
			}
		}
		return userInfo;
	}

	@Override
	public List<RoleBean> getRolesForUser(String userid)
	{
		return remoteUserService.getRolesForUser(userid);
	}

	@Override
	public List<String> getGroupIdsContainingUser(String userid)
	{
		return remoteUserService.getGroupIdsContainingUser(userid);
	}

	@Override
	public List<UserBean> getUsersInGroup(String groupId, boolean recursive)
	{
		return remoteUserService.getUsersInGroup(groupId, recursive);
	}

	@Override
	public List<GroupBean> getGroupsContainingUser(String userid)
	{
		return remoteUserService.getGroupsContainingUser(userid);
	}

	@Override
	public List<UserBean> searchUsers(String query)
	{
		return remoteUserService.searchUsers(query);
	}

	@Override
	public List<UserBean> searchUsers(String query, String parentGroupID, boolean recurse)
	{
		return remoteUserService.searchUsers(query, parentGroupID, recurse);
	}

	@Override
	public GroupBean getInformationForGroup(String uuid)
	{
		GroupBean groupInfo = GROUP_CACHE.getIfPresent(uuid);
		if( groupInfo == null )
		{
			groupInfo = remoteUserService.getInformationForGroup(uuid);
			if( groupInfo != null )
			{
				GROUP_CACHE.put(uuid, groupInfo);
			}
		}
		return groupInfo;
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIDs)
	{
		return remoteUserService.getInformationForGroups(groupIDs);
	}

	@Override
	public List<GroupBean> searchGroups(String query)
	{
		return remoteUserService.searchGroups(query);
	}

	@Override
	public List<GroupBean> searchGroups(String query, String parentId)
	{
		return remoteUserService.searchGroups(query, parentId);
	}

	@Override
	public RoleBean getInformationForRole(String uuid)
	{
		RoleBean roleInfo = ROLE_CACHE.getIfPresent(uuid);
		if( roleInfo == null )
		{
			roleInfo = remoteUserService.getInformationForRole(uuid);
			if( roleInfo != null )
			{
				ROLE_CACHE.put(uuid, roleInfo);
			}
		}

		return roleInfo;
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs)
	{
		return remoteUserService.getInformationForRoles(roleIDs);
	}

	@Override
	public List<RoleBean> searchRoles(String query)
	{
		return remoteUserService.searchRoles(query);
	}

	@Override
	public GroupBean getParentGroupForGroup(String groupID)
	{
		return remoteUserService.getParentGroupForGroup(groupID);
	}

	@Override
	public void keepAlive()
	{
		remoteUserService.keepAlive();
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		return remoteUserService.getTokenSecretIds();
	}

	@Override
	public UserManagementSettings getPluginConfig(String settingsConfig)
	{
		return remoteUserService.getPluginConfig(settingsConfig);
	}

	@Override
	public void setPluginConfig(UserManagementSettings config)
	{
		remoteUserService.setPluginConfig(config);
	}

	@Override
	public void removeFromCache(String userid)
	{
		USER_CACHE.invalidate(userid);

	}
}

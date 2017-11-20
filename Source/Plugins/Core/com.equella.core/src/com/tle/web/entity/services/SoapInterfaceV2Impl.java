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

package com.tle.web.entity.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemTaskId;
import com.tle.beans.user.TLEGroup;
import com.tle.beans.user.TLEUser;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.core.usermanagement.standard.service.TLEUserService;

/**
 * @author jmaginnis
 */
@Bind
@Singleton
public class SoapInterfaceV2Impl extends AbstractSoapService implements com.tle.core.remoting.SoapInterfaceV2
{
	@Inject
	private TLEUserService tleUserService;
	@Inject
	private TLEGroupService tleGroupService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemOperationFactory operationFactory;

	@Override
	public String addUser(String ssid, String uuid, String username, String password, String firstName, String lastName,
		String email)
	{
		authenticate(ssid);
		TLEUser user = new TLEUser();
		user.setUuid(uuid);
		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmailAddress(email);
		user.setPassword(password);

		return tleUserService.add(user);
	}

	@Override
	public String editUser(String ssid, String uuid, String username, String password, String first, String last,
		String email)
	{
		authenticate(ssid);
		return tleUserService.edit(uuid, username, password, first, last, email);
	}

	@Override
	public User getUser(String ssid, String uuid)
	{
		authenticate(ssid);
		return convert(tleUserService.get(uuid));
	}

	@Override
	public void removeUser(String ssid, String uuid)
	{
		authenticate(ssid);
		tleUserService.delete(uuid);
	}

	@Override
	public void addUserToGroup(String ssid, String uuid, String groupid)
	{
		authenticate(ssid);
		tleGroupService.addUserToGroup(groupid, uuid);
	}

	@Override
	public void removeUserFromGroup(String ssid, String uuid, String groupid)
	{
		authenticate(ssid);
		tleGroupService.removeUserFromGroup(groupid, uuid);
	}

	@Override
	public void removeUserFromAllGroups(String ssid, String userUuid)
	{
		authenticate(ssid);
		for( TLEGroup group : tleGroupService.getGroupsContainingUser(userUuid, false) )
		{
			tleGroupService.removeUserFromGroup(group.getUuid(), userUuid);
		}
	}

	@Override
	public boolean isUserInGroup(String ssid, String uuid, String groupid)
	{
		authenticate(ssid);
		return tleGroupService.getUsersInGroup(groupid, false).contains(uuid);
	}

	@Override
	public String acceptTask(String ssid, String itemUuid, int itemVersion, String taskId, boolean unlock)
		throws Exception
	{
		authenticate(ssid);
		return acceptTask(ssid, new ItemTaskId(itemUuid, itemVersion, taskId), unlock);
	}

	private String acceptTask(String ssid, ItemTaskId itemTaskId, boolean unlock) throws Exception
	{
		authenticate(ssid);
		itemService.operation(itemTaskId, operationFactory.accept(itemTaskId.getTaskId(), null, null),
			operationFactory.status(), operationFactory.saveUnlock(unlock));
		return itemTaskId.getTaskId();
	}

	@Override
	public String rejectTask(String ssid, String itemUuid, int itemVersion, String taskId, String rejectMessage,
		String toStep, boolean unlock) throws Exception
	{
		authenticate(ssid);
		return rejectTask(ssid, new ItemTaskId(itemUuid, itemVersion, taskId), rejectMessage, toStep, unlock);
	}

	private String rejectTask(String ssid, ItemTaskId itemTaskId, String rejectMessage, String toStep, boolean unlock)
		throws Exception
	{
		authenticate(ssid);
		itemService.operation(itemTaskId, operationFactory.reject(itemTaskId.getTaskId(), rejectMessage, toStep, null),
			operationFactory.status(), operationFactory.saveUnlock(unlock));
		return itemTaskId.getTaskId();
	}

	private User convert(TLEUser user)
	{
		if( user == null )
		{
			return null;
		}
		User u = new User();
		u.setEmail(user.getEmailAddress());
		u.setFirstName(user.getFirstName());
		u.setLastName(user.getLastName());
		u.setUuid(user.getUniqueID());
		u.setUsername(user.getUsername());
		return u;
	}

	@Override
	public boolean userExists(String ssid, String userUuid)
	{
		authenticate(ssid);
		try
		{
			return tleUserService.get(userUuid) != null;
		}
		catch( NotFoundException nfe )
		{
			return false;
		}
	}

	@Override
	public boolean groupExists(String ssid, String groupUuid)
	{
		authenticate(ssid);
		return tleGroupService.get(groupUuid) != null;
	}

	@Override
	public String getGroupUuidForName(String ssid, String groupName)
	{
		authenticate(ssid);
		TLEGroup groupByName = tleGroupService.getByName(groupName);
		if( groupByName != null )
		{
			return groupByName.getUuid();
		}
		return null;
	}

	@Override
	public void addGroup(String ssid, String groupId, String groupName)
	{
		authenticate(ssid);
		tleGroupService.add(tleGroupService.createGroup(groupId, groupName));
	}

	@Override
	public void removeGroup(String ssid, String groupId)
	{
		authenticate(ssid);
		tleGroupService.delete(groupId, false);
	}

	@Override
	public void removeAllUsersFromGroup(String ssid, String groupId)
	{
		tleGroupService.removeAllUsersFromGroup(groupId);

	}

	@Override
	public boolean userNameExists(String ssid, String loginName)
	{
		authenticate(ssid);
		return tleUserService.getByUsername(loginName) != null;
	}

}

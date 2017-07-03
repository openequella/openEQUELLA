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

package com.tle.core.usermanagement.soap;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.user.TLEGroup;
import com.tle.beans.user.TLEUser;
import com.tle.core.guice.Bind;
import com.tle.core.soap.service.SoapXMLService;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.core.usermanagement.standard.service.TLEUserService;

@Bind
@Singleton
public class TLEUserManagementSoapService implements TLEUserManagementSoapInterface51
{
	@Inject
	private TLEUserService tleUserService;
	@Inject
	private TLEGroupService tleGroupService;
	@Inject
	private SoapXMLService soapXML;

	@Override
	public String addUser(String uuid, String username, String password, String firstName, String lastName,
		String email)
	{
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
	public String editUser(String uuid, String username, String password, String first, String last, String email)
	{
		return tleUserService.edit(uuid, username, password, first, last, email);
	}

	@Override
	public String getUser(String uuid)
	{
		return soapXML.convertUserToXML(tleUserService.get(uuid)).toString();
	}

	@Override
	public void deleteUser(String uuid)
	{
		tleUserService.delete(uuid);
	}

	@Override
	public void addUserToGroup(String uuid, String groupid)
	{
		tleGroupService.addUserToGroup(groupid, uuid);
	}

	@Override
	public void removeUserFromGroup(String uuid, String groupid)
	{
		tleGroupService.removeUserFromGroup(groupid, uuid);
	}

	@Override
	public void removeUserFromAllGroups(String userUuid)
	{
		for( TLEGroup group : tleGroupService.getGroupsContainingUser(userUuid, false) )
		{
			tleGroupService.removeUserFromGroup(group.getUuid(), userUuid);
		}
	}

	@Override
	public boolean isUserInGroup(String uuid, String groupid)
	{
		return tleGroupService.getUsersInGroup(groupid, false).contains(uuid);
	}

	@Override
	public boolean userExists(String userUuid)
	{
		return tleUserService.get(userUuid) != null;
	}

	@Override
	public boolean groupExists(String groupUuid)
	{
		return tleGroupService.get(groupUuid) != null;
	}

	@Override
	public String getGroupUuidForName(String groupName)
	{
		TLEGroup groupByName = tleGroupService.getByName(groupName);
		if( groupByName != null )
		{
			return groupByName.getUuid();
		}
		return null;
	}

	@Override
	public void addGroup(String groupId, String groupName)
	{
		tleGroupService.add(tleGroupService.createGroup(groupId, groupName));
	}

	@Override
	public void deleteGroup(String groupId)
	{
		tleGroupService.delete(groupId, false);
	}

	@Override
	public void removeAllUsersFromGroup(String groupId)
	{
		tleGroupService.removeAllUsersFromGroup(groupId);
	}

	@Override
	public boolean userNameExists(String loginName)
	{
		return tleUserService.getByUsername(loginName) != null;
	}

	@Override
	public String searchUsersByGroup(String groupUuid, String searchString)
	{
		List<TLEUser> searchUsers = tleUserService.searchUsers(tleUserService.prepareQuery(searchString), groupUuid,
			true);
		PropBagEx users = new PropBagEx().newSubtree("users");
		for( TLEUser user : searchUsers )
		{
			users.append("", soapXML.convertUserToXML(user));
		}
		return users.toString();
	}

	@Override
	public String searchGroups(String searchString)
	{
		List<TLEGroup> searchGroups = tleGroupService.search(tleUserService.prepareQuery(searchString));
		PropBagEx groups = new PropBagEx().newSubtree("groups");
		for( TLEGroup group : searchGroups )
		{
			groups.append("", soapXML.convertGroupToXML(group));
		}
		return groups.toString();
	}

	@Override
	public String getGroupsByUser(String userUuid)
	{
		List<TLEGroup> groupsContainingUser = tleGroupService.getGroupsContainingUser(userUuid, true);
		PropBagEx groups = new PropBagEx().newSubtree("groups");
		for( TLEGroup group : groupsContainingUser )
		{
			groups.append("", soapXML.convertGroupToXML(group));
		}
		return groups.toString();
	}

	@Override
	public void editGroup(String groupUuid, String groupName)
	{
		TLEGroup editedGroup = tleGroupService.get(groupUuid);
		editedGroup.setName(groupName);
		tleGroupService.edit(editedGroup);
	}

	@Override
	public void setParentGroupForGroup(String parentGroupUuid, String groupUuid)
	{
		TLEGroup group = tleGroupService.get(groupUuid);
		TLEGroup parentGroup = tleGroupService.get(parentGroupUuid);
		group.setParent(parentGroup);
		tleGroupService.edit(group);
	}
}
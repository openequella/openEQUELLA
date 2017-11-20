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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.TLEUser;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.usermanagement.standard.service.TLEUserService;
import com.tle.common.usermanagement.user.DefaultUserState;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.plugins.ump.AbstractUserDirectory;

@Bind
public class TLEUserWrapper extends AbstractUserDirectory
{
	@Inject
	private TLEUserService tleUserService;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		return false;
	}

	@Override
	public ModifiableUserState authenticateUser(final String username, final String password)
	{
		TLEUser user = tleUserService.getByUsername(username);
		if( user != null && tleUserService.checkPasswordMatch(user, password) )
		{
			return createState(user);
		}
		return null;
	}

	private ModifiableUserState createState(TLEUser user)
	{
		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(convert(user));
		state.setInternal(true);
		return state;
	}

	private UserBean convert(TLEUser user)
	{
		if( user == null )
		{
			return null;
		}
		else
		{
			return new DefaultUserBean(user.getUuid(), user.getUsername(), user.getFirstName(), user.getLastName(),
				user.getEmailAddress());
		}
	}

	@Override
	public UserBean getInformationForUser(final String userID)
	{
		return convert(tleUserService.get(userID));
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(final Collection<String> userIds)
	{
		Map<String, UserBean> rv = Maps.newHashMapWithExpectedSize(userIds.size());
		for( TLEUser tleu : tleUserService.getInformationForUsers(userIds) )
		{
			UserBean ub = convert(tleu);
			rv.put(ub.getUniqueID(), ub);
		}
		return rv;
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(final String username, String privateData)
	{
		TLEUser user = tleUserService.getByUsername(username);
		if( user != null )
		{
			return createState(user);
		}
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		Collection<UserBean> users = new ArrayList<UserBean>();
		for( TLEUser user : tleUserService.searchUsers(query, null, false) )
		{
			users.add(new DefaultUserBean(user.getUuid(), user.getUsername(), user.getFirstName(), user.getLastName(),
				user.getEmailAddress()));
		}
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, users);
	}
}

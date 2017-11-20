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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.AbstractSystemUserWrapperSettings;
import com.tle.common.Pair;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.SystemUserState;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.exceptions.BadCredentialsException;
import com.tle.plugins.ump.AbstractUserDirectory;

@SuppressWarnings("nls")
public abstract class AbstractSystemUserWrapper extends AbstractUserDirectory
{
	private String systemUsername;
	private UserBean systemUserBean;

	protected abstract boolean authenticatePassword(String suppliedPassword);

	protected abstract String getEmailAddress();

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		this.systemUsername = ((AbstractSystemUserWrapperSettings) settings).getUsername();
		this.systemUserBean = new DefaultUserBean(systemUsername, systemUsername, systemUsername, systemUsername,
			getEmailAddress());

		return false;
	}

	@Override
	public ModifiableUserState authenticateUser(String username, String password)
	{
		if( !username.startsWith(systemUsername) )
		{
			return null;
		}

		if( authenticatePassword(password) )
		{
			return createAuthenticatedState(username);
		}

		throw newBadCredentialsException();
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(String username, String privateData)
	{
		if( !username.startsWith(systemUsername) )
		{
			return null;
		}

		return createAuthenticatedState(username);
	}

	private ModifiableUserState createAuthenticatedState(String username)
	{
		final int sysUserLen = systemUsername.length();

		if( username.length() == sysUserLen )
		{
			return new SystemUserState(CurrentInstitution.get(), systemUserBean);
		}
		else if( username.charAt(sysUserLen) == '\\' )
		{
			// Impersonate username after supportUsername + '\'
			return getChain().authenticateUserFromUsername(username.substring(sysUserLen + 1), null);
		}

		throw newBadCredentialsException();
	}

	private BadCredentialsException newBadCredentialsException()
	{
		return new BadCredentialsException("Username or password incorrect");
	}

	@Override
	public UserBean getInformationForUser(String userID)
	{
		if( isSystemUser(userID) )
		{
			return systemUserBean;
		}
		return null;
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userIDs)
	{
		for( String userID : userIDs )
		{
			if( isSystemUser(userID) )
			{
				return Collections.singletonMap(systemUsername, systemUserBean);
			}
		}
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(String userID)
	{
		return isSystemUser(userID) ? new Pair<ChainResult, Collection<GroupBean>>(ChainResult.STOP, null) : null;
	}

	@Override
	public Pair<ChainResult, Collection<RoleBean>> getRolesForUser(String userID)
	{
		return isSystemUser(userID) ? new Pair<ChainResult, Collection<RoleBean>>(ChainResult.STOP, null) : null;
	}

	private boolean isSystemUser(String userId)
	{
		return Objects.equals(userId, systemUsername);
	}
}

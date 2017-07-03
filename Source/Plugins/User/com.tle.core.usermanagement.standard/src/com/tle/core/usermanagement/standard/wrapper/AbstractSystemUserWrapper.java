/*
 * Created on Mar 16, 2005
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

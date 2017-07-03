/*
 * Created on Mar 21, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import java.util.Collections;
import java.util.Set;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.SuspendedUserWrapperSettings;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.exceptions.DisabledException;
import com.tle.plugins.ump.AbstractUserDirectory;

@Bind
public class SuspendedUserWrapper extends AbstractUserDirectory
{
	private Set<String> suspendedUsers;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		this.suspendedUsers = ((SuspendedUserWrapperSettings) settings).getSuspendedUsers();
		if( suspendedUsers == null )
		{
			suspendedUsers = Collections.emptySet();
		}
		return false;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		if( suspendedUsers.contains(state.getUserBean().getUniqueID()) )
		{
			throw new DisabledException("User account has been suspended");
		}
	}
}

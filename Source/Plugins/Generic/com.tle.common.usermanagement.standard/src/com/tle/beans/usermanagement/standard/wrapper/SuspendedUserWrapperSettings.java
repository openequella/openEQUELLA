/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import java.util.HashSet;
import java.util.Set;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;
import com.tle.common.property.annotation.PropertyList;

public class SuspendedUserWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@Property(key = "wrapper.suspended.enabled")
	private boolean enabled;

	@PropertyList(key = "wrapper.suspended.suspensions")
	private Set<String> suspendedUsers = new HashSet<String>();

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Set<String> getSuspendedUsers()
	{
		return suspendedUsers;
	}

	public void setSuspendedUsers(Set<String> suspendedUsers)
	{
		this.suspendedUsers = suspendedUsers;
	}
}

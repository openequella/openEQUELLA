/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.ump.RoleMapping;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;

public class RoleWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@PropertyDataList(key = "wrapper.role.roles", type = RoleMapping.class)
	private List<RoleMapping> roles = new ArrayList<RoleMapping>();

	@Property(key = "wrapper.role.enabled")
	private boolean enabled;

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

	public List<RoleMapping> getRoles()
	{
		return roles;
	}

	public void setRoles(List<RoleMapping> roles)
	{
		this.roles = roles;
	}
}

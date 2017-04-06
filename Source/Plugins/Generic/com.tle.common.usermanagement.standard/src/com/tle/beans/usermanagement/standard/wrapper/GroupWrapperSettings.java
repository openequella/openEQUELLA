/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;

public class GroupWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@Property(key = "wrapper.group.enabled")
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
}

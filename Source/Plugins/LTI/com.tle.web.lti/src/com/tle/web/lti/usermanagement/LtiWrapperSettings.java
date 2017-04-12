package com.tle.web.lti.usermanagement;

import com.tle.beans.ump.UserManagementSettings;

public class LtiWrapperSettings extends UserManagementSettings
{
	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		// IGNORE
	}
}

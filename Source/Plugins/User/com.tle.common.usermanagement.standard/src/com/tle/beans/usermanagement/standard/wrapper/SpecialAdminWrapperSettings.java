/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import com.tle.common.settings.annotation.Property;

public class SpecialAdminWrapperSettings extends AbstractSystemUserWrapperSettings
{
	private static final long serialVersionUID = 1L;

	public static final String TLE_ADMINISTRATOR = "TLE_ADMINISTRATOR"; //$NON-NLS-1$

	@Property(key = "wrapper.admin.username")
	private String username;

	public SpecialAdminWrapperSettings()
	{
		username = TLE_ADMINISTRATOR;
	}

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

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override
	public void setUsername(String username)
	{
		this.username = username;
	}
}

/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import com.tle.common.settings.annotation.Property;

public class RemoteSupportSettings extends AbstractSystemUserWrapperSettings
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_USERNAME = "TLE_SUPPORT"; //$NON-NLS-1$

	@Property(key = "wrapper.remotesupport.enabled")
	private boolean enabled;
	@Property(key = "wrapper.remotesupport.username")
	private String username;

	public RemoteSupportSettings()
	{
		this.username = DEFAULT_USERNAME;
	}

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

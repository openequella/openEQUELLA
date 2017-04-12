package com.tle.beans.usermanagement.leap.wrapper;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;

/**
 * @author aholland
 */
public class LeapUserWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@Property(key = "wrapper.leap.endpointurl")
	private String endpointUrl;
	@Property(key = "wrapper.leap.enabled")
	private boolean enabled;

	public String getEndpointUrl()
	{
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl)
	{
		this.endpointUrl = endpointUrl;
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
}

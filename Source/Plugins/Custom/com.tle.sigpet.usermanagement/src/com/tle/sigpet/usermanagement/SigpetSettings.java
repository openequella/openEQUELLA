/*
 * Created on 15/12/2005
 */
package com.tle.sigpet.usermanagement;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;

public class SigpetSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@Property(key = "sigpet.endpoint")
	private String endpoint;
	@Property(key = "sigpet.enabled")
	private boolean enabled;

	public String getEndpoint()
	{
		return endpoint;
	}

	public void setEndpoint(String endpoint)
	{
		this.endpoint = endpoint;
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

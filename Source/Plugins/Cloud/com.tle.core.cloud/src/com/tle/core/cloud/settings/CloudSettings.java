package com.tle.core.cloud.settings;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

public class CloudSettings implements ConfigurationProperties
{
	@Property(key = "cloud.disabled")
	private boolean disabled;

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
}
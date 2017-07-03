/*
 * Created on 15/12/2005
 */
package com.tle.beans.ump;

import com.tle.common.settings.ConfigurationProperties;

/**
 * @author Nicholas Read
 */
public abstract class UserManagementSettings implements ConfigurationProperties
{
	public abstract boolean isEnabled();

	public abstract void setEnabled(boolean enabled);
}

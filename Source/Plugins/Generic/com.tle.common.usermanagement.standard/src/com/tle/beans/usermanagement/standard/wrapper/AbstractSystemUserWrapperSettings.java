/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import com.tle.beans.ump.UserManagementSettings;

public abstract class AbstractSystemUserWrapperSettings extends UserManagementSettings
{
	public abstract String getUsername();

	public abstract void setUsername(String username);
}

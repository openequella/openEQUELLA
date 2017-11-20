/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.beans.usermanagement.standard.wrapper;

import java.util.HashSet;
import java.util.Set;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyList;

public class SuspendedUserWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@Property(key = "wrapper.suspended.enabled")
	private boolean enabled;

	@PropertyList(key = "wrapper.suspended.suspensions")
	private Set<String> suspendedUsers = new HashSet<String>();

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

	public Set<String> getSuspendedUsers()
	{
		return suspendedUsers;
	}

	public void setSuspendedUsers(Set<String> suspendedUsers)
	{
		this.suspendedUsers = suspendedUsers;
	}
}

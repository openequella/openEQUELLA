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

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.ump.RoleMapping;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;

public class RoleWrapperSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	@PropertyDataList(key = "wrapper.role.roles", type = RoleMapping.class)
	private List<RoleMapping> roles = new ArrayList<RoleMapping>();

	@Property(key = "wrapper.role.enabled")
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

	public List<RoleMapping> getRoles()
	{
		return roles;
	}

	public void setRoles(List<RoleMapping> roles)
	{
		this.roles = roles;
	}
}

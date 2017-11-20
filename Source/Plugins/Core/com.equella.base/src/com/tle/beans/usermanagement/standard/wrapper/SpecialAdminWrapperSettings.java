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

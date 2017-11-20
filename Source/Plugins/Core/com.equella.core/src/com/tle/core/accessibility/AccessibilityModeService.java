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

package com.tle.core.accessibility;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AccessibilityModeService
{
	private static final String KEY_DATABASE = "accessibility.mode"; // database

	@Inject
	private UserPreferenceService userPreferenceService;

	public boolean isAccessibilityMode()
	{
		if( CurrentInstitution.get() == null )
		{
			return false;
		}
		UserState userState = CurrentUser.getUserState();
		if( userState == null )
		{
			return false;
		}
		Boolean acMode = userState.getCachedAttribute(AccessibilityModeService.class);
		if( acMode != null )
		{
			return acMode;
		}
		acMode = "true".equals(userPreferenceService.getPreference(KEY_DATABASE));
		userState.setCachedAttribute(AccessibilityModeService.class, acMode);
		return acMode;

	}

	// public method that sets database and clears session
	public void setAccessibilityMode(boolean acMode)
	{
		userPreferenceService.setPreference(KEY_DATABASE, Boolean.toString(acMode));
		UserState userState = CurrentUser.getUserState();
		userState.removeCachedAttribute(AccessibilityModeService.class);
	}
}

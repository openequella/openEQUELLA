package com.tle.core.accessibility;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

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

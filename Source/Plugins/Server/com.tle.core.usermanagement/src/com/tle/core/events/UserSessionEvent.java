package com.tle.core.events;

import com.tle.core.user.UserState;

/**
 * @author aholland
 */
public interface UserSessionEvent
{
	String getSessionId();

	UserState getUserState();
}

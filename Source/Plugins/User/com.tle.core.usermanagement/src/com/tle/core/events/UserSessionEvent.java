package com.tle.core.events;

import com.tle.common.usermanagement.user.UserState;

/**
 * @author aholland
 */
public interface UserSessionEvent
{
	String getSessionId();

	UserState getUserState();
}

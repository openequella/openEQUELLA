package com.tle.core.events.listeners;

import com.tle.core.events.UserSessionLogoutEvent;

/**
 * @author Nicholas Read
 */
public interface UserSessionLogoutListener extends ApplicationListener
{
	void userSessionDestroyedEvent(UserSessionLogoutEvent event);
}

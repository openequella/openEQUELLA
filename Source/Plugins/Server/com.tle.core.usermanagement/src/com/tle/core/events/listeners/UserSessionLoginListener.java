package com.tle.core.events.listeners;

import com.tle.core.events.UserSessionLoginEvent;

/**
 * @author Nicholas Read
 */
public interface UserSessionLoginListener extends ApplicationListener
{
	void userSessionCreatedEvent(UserSessionLoginEvent event);
}

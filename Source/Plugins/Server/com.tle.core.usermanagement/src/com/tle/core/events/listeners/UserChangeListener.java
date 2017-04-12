package com.tle.core.events.listeners;

import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;

/**
 * @author Nicholas Read
 */
public interface UserChangeListener extends ApplicationListener
{
	void userDeletedEvent(UserDeletedEvent event);

	void userEditedEvent(UserEditEvent event);

	void userIdChangedEvent(UserIdChangedEvent event);
}

package com.tle.core.events.listeners;

import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;

/**
 * @author Nicholas Read
 */
public interface GroupChangedListener extends ApplicationListener
{
	void groupDeletedEvent(GroupDeletedEvent event);

	void groupEditedEvent(GroupEditEvent event);
	
	void groupIdChangedEvent(GroupIdChangedEvent event);
}

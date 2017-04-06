package com.tle.core.events.listeners;

import com.tle.core.events.ItemDeletedEvent;

/**
 * @author Nicholas Read
 */
public interface ItemDeletedListener extends ApplicationListener
{
	void itemDeletedEvent(ItemDeletedEvent event);
}

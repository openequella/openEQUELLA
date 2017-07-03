package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.ItemDeletedEvent;

/**
 * @author Nicholas Read
 */
public interface ItemDeletedListener extends ApplicationListener
{
	void itemDeletedEvent(ItemDeletedEvent event);
}

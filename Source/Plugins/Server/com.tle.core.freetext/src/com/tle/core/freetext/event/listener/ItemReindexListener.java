package com.tle.core.freetext.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.freetext.event.ItemReindexEvent;

/**
 * @author Nicholas Read
 */
public interface ItemReindexListener extends ApplicationListener
{
	void itemReindexEvent(ItemReindexEvent event);
}

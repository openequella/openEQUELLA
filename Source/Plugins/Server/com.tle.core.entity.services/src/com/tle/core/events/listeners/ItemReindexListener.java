package com.tle.core.events.listeners;

import com.tle.core.events.ItemReindexEvent;

/**
 * @author Nicholas Read
 */
public interface ItemReindexListener extends ApplicationListener
{
	void itemReindexEvent(ItemReindexEvent event);
}

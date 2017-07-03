package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.ItemOperationEvent;

/**
 * @author Nicholas Read
 */
public interface ItemOperationListener extends ApplicationListener
{
	void itemOperationEvent(ItemOperationEvent event);
}

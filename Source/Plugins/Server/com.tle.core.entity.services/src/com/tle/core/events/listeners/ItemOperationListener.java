package com.tle.core.events.listeners;

import com.tle.core.events.ItemOperationEvent;

/**
 * @author Nicholas Read
 */
public interface ItemOperationListener extends ApplicationListener
{
	void itemOperationEvent(ItemOperationEvent event);
}

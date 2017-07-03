package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.ItemOperationBatchEvent;

/**
 * @author Aaron
 */
public interface ItemOperationBatchListener extends ApplicationListener
{
	void itemOperationBatchEvent(ItemOperationBatchEvent event);
}

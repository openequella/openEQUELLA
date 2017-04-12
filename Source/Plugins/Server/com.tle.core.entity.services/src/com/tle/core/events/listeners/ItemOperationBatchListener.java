package com.tle.core.events.listeners;

import com.tle.core.events.ItemOperationBatchEvent;

/**
 * @author Aaron
 */
public interface ItemOperationBatchListener extends ApplicationListener
{
	void itemOperationBatchEvent(ItemOperationBatchEvent event);
}

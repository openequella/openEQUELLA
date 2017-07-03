package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.UnindexItemEvent;

/**
 * @author Nicholas Read
 */
public interface UnindexItemListener extends ApplicationListener
{
	void unindexItemEvent(UnindexItemEvent event);
}

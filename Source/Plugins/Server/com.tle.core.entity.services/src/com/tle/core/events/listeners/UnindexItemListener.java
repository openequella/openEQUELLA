package com.tle.core.events.listeners;

import com.tle.core.events.UnindexItemEvent;

/**
 * @author Nicholas Read
 */
public interface UnindexItemListener extends ApplicationListener
{
	void unindexItemEvent(UnindexItemEvent event);
}

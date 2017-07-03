package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.IndexItemNowEvent;

/**
 * @author Nicholas Read
 */
public interface IndexItemNowListener extends ApplicationListener
{
	void indexItemNowEvent(IndexItemNowEvent event);
}

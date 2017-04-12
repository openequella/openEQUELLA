package com.tle.core.events.listeners;

import com.tle.core.events.IndexItemNowEvent;

/**
 * @author Nicholas Read
 */
public interface IndexItemNowListener extends ApplicationListener
{
	void indexItemNowEvent(IndexItemNowEvent event);
}

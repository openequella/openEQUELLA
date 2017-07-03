package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.IndexItemBackgroundEvent;

/**
 * @author Nicholas Read
 */
public interface IndexItemBackgroundListener extends ApplicationListener
{
	void indexItemBackgroundEvent(IndexItemBackgroundEvent event);
}

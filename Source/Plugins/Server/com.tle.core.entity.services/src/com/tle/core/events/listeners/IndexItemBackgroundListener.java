package com.tle.core.events.listeners;

import com.tle.core.events.IndexItemBackgroundEvent;

/**
 * @author Nicholas Read
 */
public interface IndexItemBackgroundListener extends ApplicationListener
{
	void indexItemBackgroundEvent(IndexItemBackgroundEvent event);
}

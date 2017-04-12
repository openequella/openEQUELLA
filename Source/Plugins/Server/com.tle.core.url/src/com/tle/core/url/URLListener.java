package com.tle.core.url;

import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public interface URLListener extends ApplicationListener
{
	void urlEvent(URLEvent event);
}

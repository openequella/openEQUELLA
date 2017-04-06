package com.tle.core.events.listeners;

import com.tle.core.events.UMPChangedEvent;

/**
 * @author Nicholas Read
 */
public interface UMPChangedListener extends ApplicationListener
{
	void umpChangedEvent(UMPChangedEvent event);
}

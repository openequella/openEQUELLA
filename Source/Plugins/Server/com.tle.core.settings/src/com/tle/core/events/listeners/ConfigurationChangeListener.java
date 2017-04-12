package com.tle.core.events.listeners;

import com.tle.core.events.ConfigurationChangedEvent;

/**
 * @author Nicholas Read
 */
public interface ConfigurationChangeListener extends ApplicationListener
{
	void configurationChangedEvent(ConfigurationChangedEvent event);
}

package com.tle.core.settings.events.listeners;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.settings.events.ConfigurationChangedEvent;

/**
 * @author Nicholas Read
 */
public interface ConfigurationChangeListener extends ApplicationListener
{
	void configurationChangedEvent(ConfigurationChangedEvent event);
}

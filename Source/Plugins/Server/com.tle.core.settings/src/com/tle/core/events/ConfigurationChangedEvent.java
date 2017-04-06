package com.tle.core.events;

import com.tle.core.events.listeners.ConfigurationChangeListener;

/**
 * @author Nicholas Read
 */
public class ConfigurationChangedEvent extends ApplicationEvent<ConfigurationChangeListener>
{
	private static final long serialVersionUID = 1L;

	public ConfigurationChangedEvent()
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
	}

	@Override
	public Class<ConfigurationChangeListener> getListener()
	{
		return ConfigurationChangeListener.class;
	}

	@Override
	public void postEvent(ConfigurationChangeListener listener)
	{
		listener.configurationChangedEvent(this);
	}
}

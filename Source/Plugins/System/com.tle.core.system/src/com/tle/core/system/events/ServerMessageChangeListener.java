package com.tle.core.system.events;

import com.tle.core.events.listeners.ApplicationListener;

public interface ServerMessageChangeListener extends ApplicationListener
{
	void serverMessageChangedEvent(ServerMessageChangedEvent event);
}

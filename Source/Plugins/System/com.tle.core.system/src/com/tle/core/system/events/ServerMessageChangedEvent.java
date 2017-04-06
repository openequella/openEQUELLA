package com.tle.core.system.events;

import com.tle.core.events.ApplicationEvent;

public class ServerMessageChangedEvent extends ApplicationEvent<ServerMessageChangeListener>
{
	public ServerMessageChangedEvent()
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
	}

	@Override
	public Class<ServerMessageChangeListener> getListener()
	{
		return ServerMessageChangeListener.class;
	}

	@Override
	public void postEvent(ServerMessageChangeListener listener)
	{
		listener.serverMessageChangedEvent(this);
	}
}

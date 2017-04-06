package com.tle.tomcat.events;

import com.tle.core.events.ApplicationEvent;

public class TomcatRestartEvent extends ApplicationEvent<TomcatRestartListener>
{
	public TomcatRestartEvent()
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
	}

	@Override
	public Class<TomcatRestartListener> getListener()
	{
		return TomcatRestartListener.class;
	}

	@Override
	public void postEvent(TomcatRestartListener listener)
	{
		listener.restartTomcat();
	}
}

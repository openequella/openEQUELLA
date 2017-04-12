package com.tle.web.listeners;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

public class UserSessionDestructionListener implements HttpSessionListener
{
	private PluginTracker<HttpSessionListener> listenerTracker;

	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
		for( HttpSessionListener listener : getListeners() )
		{
			listener.sessionCreated(event);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		for( HttpSessionListener listener : getListeners() )
		{
			listener.sessionDestroyed(event);
		}
	}

	@SuppressWarnings("nls")
	private synchronized List<HttpSessionListener> getListeners()
	{
		if( listenerTracker == null )
		{
			PluginService pluginService = AbstractPluginService.get();
			if( pluginService == null )
			{
				// We're not initialised yet, probably because Tomcat is trying
				// to tell us about a bunch of existing sessions from other
				// cluster nodes while we're still starting up.
				return Collections.emptyList();
			}

			listenerTracker = new PluginTracker<HttpSessionListener>(pluginService, "com.tle.web.core",
				"webSessionListener", null, new PluginTracker.ExtensionParamComparator("order")).setBeanKey("bean");
		}
		return listenerTracker.getBeanList();
	}
}

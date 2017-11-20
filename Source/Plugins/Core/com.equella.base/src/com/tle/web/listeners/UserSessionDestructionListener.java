/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

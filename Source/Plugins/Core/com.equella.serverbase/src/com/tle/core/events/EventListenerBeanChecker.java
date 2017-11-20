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

package com.tle.core.events;

import java.util.HashSet;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.PluginDescriptor;

import com.google.common.base.Joiner;
import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.guice.BeanChecker;
import com.tle.core.plugins.PluginService;

/**
 * Check that if a bean implements an event listener interface, that an
 * extension also exists that registers the bean as a listener for that event
 * type.
 */
@SuppressWarnings("nls")
public class EventListenerBeanChecker implements BeanChecker
{
	@Override
	public void check(PluginService pluginService, Class<?> actualClass, Set<Class<?>> interfaces)
	{
		Set<String> listeners = new HashSet<String>();
		for( Class<?> i : interfaces )
		{
			if( ApplicationListener.class != i && ApplicationListener.class.isAssignableFrom(i) )
			{
				listeners.add(i.getName());
			}
		}

		final PluginDescriptor pd = pluginService.getPluginForObject(actualClass).getDescriptor();
		for( Extension e : pd.getExtensions() )
		{
			// Short-circuit out before each iteration
			if( listeners.isEmpty() )
			{
				return;
			}

			if( e.getExtendedPluginId().equals("com.tle.core.events")
				&& e.getExtendedPointId().equals("applicationEventListener") )
			{
				Parameter lp = e.getParameter("listener");
				if( lp == null || isForThisClass(lp.valueAsString(), actualClass, pd) )
				{
					// This extension is either not restricted to any specific
					// bean, or it is for this particular bean. Any listed
					// listener classes are therefore valid for this bean, so
					// remove them from our list.
					for( Parameter lcp : e.getParameters("listenerClass") )
					{
						listeners.remove(lcp.valueAsString());
					}
				}

			}
		}

		if( !listeners.isEmpty() )
		{
			throw new RuntimeException(
				actualClass.getName() + " implements the following listener interfaces, but they are not registered"
					+ " to the com.tle.core.events@applicationEventListener extension point: "
					+ Joiner.on(',').join(listeners));
		}
	}

	private boolean isForThisClass(String listenerClass, Class<?> actualClass, PluginDescriptor pd)
	{
		if( !listenerClass.startsWith("bean:") )
		{
			throw new RuntimeException(
				"All listenerClass values should be for bean name, and that in turn should be a class name");
		}

		listenerClass = listenerClass.substring(5);

		try
		{
			return actualClass.getClassLoader().loadClass(listenerClass).isAssignableFrom(actualClass);
		}
		catch( ClassNotFoundException ex )
		{
			throw new RuntimeException("Attempting to register events for listenerClass " + listenerClass
				+ " that are not accessible in plug-in " + pd.getId());
		}
	}
}

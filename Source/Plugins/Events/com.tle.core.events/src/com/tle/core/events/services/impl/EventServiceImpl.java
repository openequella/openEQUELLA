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

package com.tle.core.events.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.cluster.ClusterMessageHandler;
import com.tle.core.cluster.service.ClusterMessagingService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.ApplicationEvent.PostTo;
import com.tle.core.events.DefaultExecutor;
import com.tle.core.events.EventExecutor;
import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

@Singleton
@SuppressWarnings("nls")
@Bind(EventService.class)
public class EventServiceImpl implements EventService, ClusterMessageHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

	@Inject
	private ClusterMessagingService clusterMessagingService;
	@Inject
	private PluginTracker<ApplicationListener> pluginListeners;
	@Inject
	private PluginTracker<EventExecutor> executorTracker;
	@Inject
	private PluginService pluginService;

	private EventExecutor executor;
	private Map<String, List<ListenerFinder>> extensionMap;
	private Map<String, Set<ApplicationListener>> extensionBeanMap;

	private EventExecutor getExecutor()
	{
		if( executor == null )
		{
			executor = executorTracker.getBeanList().get(0);
		}
		return executor;
	}

	@Override
	public Runnable canHandle(Object msg)
	{
		if( msg instanceof RemoteEvent )
		{
			final RemoteEvent re = (RemoteEvent) msg;
			return convertToRunnable(re.getInstitutionId(), re.getEvent());
		}
		return null;
	}

	private Runnable convertToRunnable(long institutionId, final ApplicationEvent<?> event)
	{
		validateEvent(event, institutionId);

		return getExecutor().createRunnable(institutionId, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					executeEventNow(event);
				}
				catch( Throwable t )
				{
					LOGGER.error("Error in executeEventNow", t);
				}
			}
		});
	}

	private void submitEvent(Institution institution, ApplicationEvent<?> event)
	{
		DefaultExecutor.executor.submit(convertToRunnable(getInstitutionId(institution), event));
	}

	private void postEventToOthers(Institution institution, ApplicationEvent<?> event)
	{
		clusterMessagingService.postMessage(new RemoteEvent(event, institution));
	}

	/**
	 * Execute and event immediately, synchronously.
	 */
	private void executeEventNow(ApplicationEvent<?> event)
	{
		// TODO: ApplicationEvent shouldn't need to know about its listeners.
		// Fix that shit.
		@SuppressWarnings("unchecked")
		ApplicationEvent<ApplicationListener> eventHack = (ApplicationEvent<ApplicationListener>) event;

		LOGGER.debug("Executing event now: " + event.getClass().getName());

		Set<ApplicationListener> listeners = getListeners(event.getListener());
		Throwable firstEx = null;
		for( ApplicationListener listener : listeners )
		{
			try
			{
				eventHack.postEvent(listener);
			}
			catch( Exception ex )
			{
				LOGGER.error("Error while posting event " + event.getClass().getName() + " to listener "
					+ listener.getClass().getName(), ex);
				if( firstEx == null )
				{
					firstEx = ex;
				}
			}
		}
		if( firstEx != null )
		{
			throw new RuntimeException(firstEx);
		}
	}

	private synchronized Set<ApplicationListener> getListeners(Class<?> clazz)
	{
		if( extensionBeanMap == null || pluginListeners.needsUpdate() )
		{
			extensionBeanMap = new HashMap<String, Set<ApplicationListener>>();
		}

		String clazzName = clazz.getName();

		Set<ApplicationListener> listeners = extensionBeanMap.get(clazzName);
		if( listeners != null )
		{
			return listeners;
		}

		listeners = new HashSet<ApplicationListener>();
		List<ListenerFinder> extList = getExtensionMap().get(clazzName);
		if( extList != null )
		{
			for( ListenerFinder finder : extList )
			{
				finder.addListeners(listeners);
			}
		}
		extensionBeanMap.put(clazzName, listeners);

		return listeners;

	}

	private synchronized Map<String, List<ListenerFinder>> getExtensionMap()
	{
		if( extensionMap == null || pluginListeners.needsUpdate() )
		{
			extensionMap = new HashMap<String, List<ListenerFinder>>();
			List<Extension> extensions = pluginListeners.getExtensions();
			for( Extension extension : extensions )
			{
				Parameter listenerParam = extension.getParameter("listener");
				Collection<Parameter> listClasses = extension.getParameters("listenerClass");
				for( Parameter listClassname : listClasses )
				{
					String listenerClass = listClassname.valueAsString();
					List<ListenerFinder> extList = extensionMap.get(listenerClass);
					if( extList == null )
					{
						extList = new ArrayList<ListenerFinder>();
						extensionMap.put(listenerClass, extList);
					}
					if( listenerParam == null )
					{
						extList.add(new ScanForListeners(listenerClass, extension));
					}
					else
					{
						extList.add(new BeanListener(listenerParam.valueAsString(), extension));
					}
				}
			}
		}
		return extensionMap;
	}

	private interface ListenerFinder
	{
		void addListeners(Set<ApplicationListener> listeners);
	}

	public class BeanListener implements ListenerFinder
	{
		private final String bean;
		private final Extension extension;

		public BeanListener(String bean, Extension extension)
		{
			this.bean = bean;
			this.extension = extension;
		}

		@Override
		public void addListeners(Set<ApplicationListener> listeners)
		{
			listeners.add((ApplicationListener) pluginService.getBean(extension.getDeclaringPluginDescriptor(), bean));
		}
	}

	public class ScanForListeners implements ListenerFinder
	{
		private final String listenerClass;
		private final Extension extension;

		public ScanForListeners(String listenerClass, Extension extension)
		{
			this.listenerClass = listenerClass;
			this.extension = extension;
		}

		@Override
		public void addListeners(Set<ApplicationListener> listeners)
		{
			PluginBeanLocator locator = pluginService.getBeanLocator(extension.getDeclaringPluginDescriptor().getId());
			try
			{
				Class<ApplicationListener> listenerClazz = locator.loadClass(listenerClass);
				listeners.addAll(locator.getBeansOfType(listenerClazz));
			}
			catch( ClassNotFoundException e )
			{
				throw new RuntimeException("Can't find the class in context:" + extension);
			}
		}
	}

	public static class RemoteEvent implements Serializable
	{
		private static final long serialVersionUID = 9140629432070365393L;

		private final ApplicationEvent<?> event;
		private final long institutionId;

		public RemoteEvent(ApplicationEvent<?> event, Institution institution)
		{
			this.event = event;
			this.institutionId = EventServiceImpl.getInstitutionId(institution);
		}

		public ApplicationEvent<?> getEvent()
		{
			return event;
		}

		public long getInstitutionId()
		{
			return institutionId;
		}
	}

	@Override
	public void publishApplicationEvent(ApplicationEvent<?> event)
	{
		publishApplicationEvent(CurrentInstitution.get(), event);
	}

	@Override
	public void publishApplicationEvent(Collection<Institution> institutions, final ApplicationEvent<?> event)
	{
		for( Institution institution : institutions )
		{
			if( event.getPostTo() == PostTo.POST_TO_SELF_SYNCHRONOUSLY )
			{
				convertToRunnable(institution.getUniqueId(), event).run();
			}
			else
			{
				publishApplicationEvent(institution, event);
			}
		}
	}

	private void publishApplicationEvent(Institution institution, ApplicationEvent<?> event)
	{
		validateEvent(event, getInstitutionId(institution));

		switch( event.getPostTo() )
		{
			case POST_TO_SELF_SYNCHRONOUSLY:
				executeEventNow(event);
				break;
			case POST_ONLY_TO_SELF:
				submitEvent(institution, event);
				break;
			case POST_TO_ALL_CLUSTER_NODES:
				submitEvent(institution, event);
				postEventToOthers(institution, event);
				break;
			case POST_TO_OTHER_CLUSTER_NODES:
				postEventToOthers(institution, event);
				break;
		}
	}

	private static long getInstitutionId(Institution institution)
	{
		return institution == null ? -1 : institution.getUniqueId();
	}

	private static void validateEvent(ApplicationEvent<?> event, long institutionId)
	{
		if( event.requiresInstitution() && institutionId < 0 )
		{
			throw new IllegalStateException(event.getClass() + " events require an institution");
		}
	}

}

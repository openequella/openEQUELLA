/*
 * Copyright 2019 Apereo
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

package com.tle.core.application.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import sun.reflect.Reflection;

import com.tle.core.plugins.AbstractBeanLocatorCallable;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PrivatePluginBeanLocator;
import com.tle.core.plugins.PrivatePluginService;

public class PluginServiceImpl extends AbstractPluginService implements PrivatePluginService
{
	private static final Log LOGGER = LogFactory.getLog(PluginServiceImpl.class);

	private final Map<String, PrivatePluginBeanLocator> lookups = new HashMap<String, PrivatePluginBeanLocator>();

	private final ExecutorService executor = Executors.newFixedThreadPool(
		Math.max(2, Runtime.getRuntime().availableProcessors()), new LocatorThreadFactory());

	public PluginServiceImpl(Set<TLEPluginLocation> alreadyRegistered)
	{
		for( TLEPluginLocation tlePluginLocation : alreadyRegistered )
		{
			pluginIdToLocation.put(tlePluginLocation.getManifestInfo().getId(), tlePluginLocation);
		}
	}

	@Override
	public boolean isPluginDisabled(TLEPluginLocation pluginLocation)
	{
		return false;
	}

	@Override
	@SuppressWarnings("nls")
	public Object getBean(PluginDescriptor plugin, String clazzName)
	{
		try
		{
			ensureActivated(plugin);
			if( clazzName.startsWith("bean:") ) //$NON-NLS-1$
			{
				clazzName = clazzName.substring(5);
				PluginBeanLocator locator = lookups.get(plugin.getId());
				Object bean = locator.getBean(clazzName);
				if( bean == null )
				{
					throw new Error("Bean " + clazzName + " not found");
				}
				return bean;
			}
			if (clazzName.startsWith("object:"))
			{
				try
				{
					Class<?> clazz = pluginManager.getPluginClassLoader(plugin).loadClass(clazzName.substring(7) + "$");
					Field module$ = clazz.getField("MODULE$");
					return module$.get(null);
				}
				catch (IllegalAccessException|NoSuchFieldException|ClassNotFoundException e)
				{
					LOGGER.error("Error creating object '" + clazzName + "' in " + plugin.getId(), e);
					throw new RuntimeException(e);
				}
			}
			return instantiatePluginClass(plugin, clazzName);
		}
		catch( RuntimeException re )
		{
			LOGGER.error("Error creating bean '" + clazzName + "' in " + plugin.getId(), re);
			throw re;
		}
	}

	@SuppressWarnings({"nls"})
	@Override
	public void ensureActivated(PluginDescriptor plugin)
	{
		if( LOGGER.isDebugEnabled() && !pluginManager.isPluginActivated(plugin) )
		{
			int i = 1;
			Class<?> callerClass = Reflection.getCallerClass(i);
			while( callerClass == PluginServiceImpl.class || callerClass == AbstractPluginService.class
				|| callerClass == PluginTracker.class )
			{
				callerClass = Reflection.getCallerClass(++i);
			}

			StringBuilder sb = new StringBuilder("Plug-in ");
			sb.append(plugin.getUniqueId());
			sb.append(" activated by ");
			sb.append(callerClass);

			Plugin p = getPluginForObject(callerClass);
			if( p != null )
			{
				sb.append(" in ");
				sb.append(p.getDescriptor().getUniqueId());

			}
			LOGGER.debug(sb.toString());
		}

		try
		{
			super.ensureActivated(plugin);
		}
		catch( RuntimeException e )
		{
			LOGGER.error("Error activating:" + e, e);
			throw e;
		}
	}

	@Override
	public void setPluginBeanLocator(String pluginId, PrivatePluginBeanLocator locator)
	{
		lookups.put(pluginId, locator);
	}

	@Override
	public PluginBeanLocator getBeanLocator(String pluginId)
	{
		return lookups.get(pluginId);
	}

	@Override
	public void initLocatorsFor(List<Extension> extensions)
	{
		Set<PrivatePluginBeanLocator> locators = new HashSet<PrivatePluginBeanLocator>();
		for( Extension extension : extensions )
		{
			PluginDescriptor descriptor = extension.getDeclaringPluginDescriptor();
			String pluginId = descriptor.getId();
			PrivatePluginBeanLocator locator = lookups.get(pluginId);
			if( locator != null )
			{
				ensureActivated(descriptor);
				locators.add(locator);
			}
		}
		ensureBeanLocators(locators);
	}

	@SuppressWarnings("nls")
	@Override
	public void ensureBeanLocators(Collection<? extends PrivatePluginBeanLocator> beanLocators)
	{
		List<PrivatePluginBeanLocator> locators = new ArrayList<PrivatePluginBeanLocator>(beanLocators);
		LinkedBlockingDeque<Object> queue = new LinkedBlockingDeque<Object>();
		boolean noCallables = false;
		while( true )
		{
			Iterator<PrivatePluginBeanLocator> iter = locators.iterator();
			while( iter.hasNext() )
			{
				PrivatePluginBeanLocator locator = iter.next();
				if( locator.isInitialised() || locator.isErrored() )
				{
					iter.remove();
				}
				else if( noCallables )
				{
					throw new RuntimeException("No work to be done but " + locator + " not initialised");
				}
			}
			if( locators.isEmpty() )
			{
				return;
			}
			List<AbstractBeanLocatorCallable<?>> callableList = new ArrayList<AbstractBeanLocatorCallable<?>>();
			Set<PrivatePluginBeanLocator> seenLocators = new HashSet<PrivatePluginBeanLocator>();
			for( PrivatePluginBeanLocator locator : locators )
			{
				try
				{
					locator.addCallables(callableList, seenLocators);
				}
				catch( Throwable t )// NOSONAR
				{
					locator.setThrowable(t);
				}
			}
			noCallables = callableList.isEmpty();
			if( callableList.size() > 0 )
			{
				// LOGGER.info("Callables " + callableList);
				for( AbstractBeanLocatorCallable<?> callable : callableList )
				{
					callable.addWaiter(queue);
					callable.submit(executor);
				}
				try
				{
					queue.take();
				}
				catch( InterruptedException e )
				{
					// ignore
				}
				queue.clear();
			}
		}
	}

	protected static class LocatorThreadFactory implements ThreadFactory
	{
		final AtomicInteger threadNumber = new AtomicInteger(1);

		@SuppressWarnings("nls")
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "LocatorPool-" + threadNumber.getAndIncrement());
		}
	}

}

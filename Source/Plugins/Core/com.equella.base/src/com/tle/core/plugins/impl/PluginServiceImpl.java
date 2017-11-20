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

package com.tle.core.plugins.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ManifestInfo;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.util.IoUtil;

import com.tle.core.plugins.AbstractBeanLocatorCallable;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PrivatePluginBeanLocator;
import com.tle.core.plugins.PrivatePluginService;

@SuppressWarnings("nls")
public class PluginServiceImpl extends AbstractPluginService implements PrivatePluginService
{
	private static final Logger LOGGER = Logger.getLogger(PluginServiceImpl.class);
	private static final String PLUGIN_JPF_XML = "plugin-jpf.xml";

	private static final Pattern JAR_PATTERN = Pattern.compile("(.*)-\\d+\\.\\d+\\.(\\d+).jar");

	private final Map<String, PrivatePluginBeanLocator> lookups = new HashMap<String, PrivatePluginBeanLocator>();

	private final Set<TLEPluginLocation> disabledPlugins = new HashSet<TLEPluginLocation>();
	private final Map<String, Boolean> jarContainsPlugin = new HashMap<String, Boolean>();
	private final Set<TLEPluginLocation> registered;

	private Set<String> disabledPluginNames = new HashSet<String>();
	private List<String> pluginFolders;

	private final ExecutorService executor = Executors.newFixedThreadPool(
		Math.max(2, Runtime.getRuntime().availableProcessors()), new LocatorThreadFactory());

	public PluginServiceImpl(Set<TLEPluginLocation> alreadyRegistered)
	{
		this.registered = alreadyRegistered;
		for( TLEPluginLocation tlePluginLocation : alreadyRegistered )
		{
			pluginIdToLocation.put(tlePluginLocation.getManifestInfo().getId(), tlePluginLocation);
		}
	}

	public void setPluginFolders(List<String> pluginFolders)
	{
		this.pluginFolders = pluginFolders;
	}

	@Override
	public boolean isPluginDisabled(TLEPluginLocation pluginLocation)
	{
		return disabledPlugins.contains(pluginLocation);
	}

	@PostConstruct
	public void startTimer()
	{
		// Timer timer = new Timer();
		TimerTask scanTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					final PluginRegistry registry = pluginManager.getRegistry();
					final Set<TLEPluginLocation> plugins = new HashSet<TLEPluginLocation>(
						scanPluginFolders(pluginFolders));
					final List<String> ids = new ArrayList<String>();

					Iterator<TLEPluginLocation> iter = registered.iterator();
					while( iter.hasNext() )
					{
						final TLEPluginLocation location = iter.next();
						if( plugins.contains(location) )
						{
							plugins.remove(location);
						}
						else
						{
							ids.add(location.getManifestInfo().getId());
							iter.remove();
						}
					}

					iter = plugins.iterator();
					while( iter.hasNext() )
					{
						TLEPluginLocation location = iter.next();
						try
						{
							final String pluginId = location.getManifestInfo().getId();
							if( disabledPluginNames.contains(pluginId) )
							{
								iter.remove();
								LOGGER.warn("Disabled plugin: " + pluginId);
								disabledPlugins.add(location);
							}
							registered.add(location);
							pluginIdToLocation.put(pluginId, location);
						}
						catch( Exception e )
						{
							LOGGER.warn("Failed to parse:" + location.getManifestLocation(), e);
						}
					}

					if( ids.size() > 0 )
					{
						registry.unregister(ids.toArray(new String[ids.size()]));
					}

					if( plugins.size() > 0 )
					{
						pluginManager.publishPlugins(plugins.toArray(new PluginLocation[plugins.size()]));
					}
				}
				catch( Exception e )
				{
					LOGGER.error("Error while looking for plugins", e);
				}
			}
		};
		scanTask.run();
		// No need to be dynamic
		// timer.schedule(scanTask, new Date(), 1000 * 10);
	}

	@Override
	public Object getBean(PluginDescriptor plugin, String clazzName)
	{
		try
		{
			ensureActivated(plugin);
			if( clazzName.startsWith("bean:") )
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

			return instantiatePluginClass(plugin, clazzName);
		}
		catch( RuntimeException re )
		{
			LOGGER.error("Error creating bean '" + clazzName + "' in " + plugin.getId(), re);
			throw re;
		}
	}

	@Override
	public void ensureActivated(PluginDescriptor plugin)// , Class<?>
														// callerClass)
	{
		// FIXME: need a new way of logging this. See #EQ-107
		// if( LOGGER.isDebugEnabled() &&
		// !pluginManager.isPluginActivated(plugin) )
		// {
		// int i = 1;
		// Class<?> callerClass = Reflection.getCallerClass(i);
		// while( callerClass == PluginServiceImpl.class || callerClass ==
		// AbstractPluginService.class
		// || callerClass == PluginTracker.class )
		// {
		// callerClass = Reflection.getCallerClass(++i);
		// }
		//
		// StringBuilder sb = new StringBuilder("Plug-in ");
		// sb.append(plugin.getUniqueId());
		// sb.append(" activated by ");
		// sb.append(callerClass);
		//
		// Plugin p = getPluginForObject(callerClass);
		// if( p != null )
		// {
		// sb.append(" in ");
		// sb.append(p.getDescriptor().getUniqueId());
		//
		// }
		// LOGGER.debug(sb.toString());
		// }

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

	public void setDisabledPlugins(String disabledPlugins)
	{
		this.disabledPluginNames = new HashSet<String>();
		List<String> disabledList = Arrays.asList(disabledPlugins.split(","));
		for( String disabled : disabledList )
		{
			this.disabledPluginNames.add(disabled.trim());
		}
	}

	private Collection<TLEPluginLocation> scanPluginFolders(Collection<String> pluginFolders) throws Exception
	{
		Map<String, TLEPluginLocation> plugins = new HashMap<String, TLEPluginLocation>();
		for( String folder : pluginFolders )
		{
			String[] folders = folder.split(",");
			for( String folderName : folders )
			{
				File file = new File(folderName.trim());
				if( file.exists() )
				{
					scanPluginFolder(file, plugins);
				}
			}
		}
		return plugins.values();
	}

	private void scanPluginFolder(File folder, Map<String, TLEPluginLocation> plugins) throws Exception
	{
		File[] files = folder.listFiles();
		if( files == null )
		{
			return;
		}

		for( File file : files )
		{
			String filename = file.getName();
			if( file.isDirectory() )
			{
				File manFile = new File(file, PLUGIN_JPF_XML);
				if( manFile.exists() )
				{
					URL context = file.toURI().toURL();
					URL manifest = new URL(context, PLUGIN_JPF_XML);
					ManifestInfo info = pluginManager.getRegistry().readManifestInfo(manifest);
					TLEPluginLocation location = new TLEPluginLocation(info, filename, context, manifest);
					plugins.put(file.getAbsolutePath(), location);
				}
				else
				{
					scanPluginFolder(file, plugins);
				}
			}
			else if( filename.endsWith(".jar") )
			{
				int version = -1;
				String pluginId = filename;
				Matcher matcher = JAR_PATTERN.matcher(filename);
				if( matcher.matches() )
				{
					pluginId = matcher.group(1);
					version = Integer.parseInt(matcher.group(2));
				}
				URL context = new URL("jar", "", file.toURI() + "!/");
				URL manFile = new URL(context, PLUGIN_JPF_XML);
				String manFileStr = manFile.toString();
				Boolean hasJpf = jarContainsPlugin.get(manFileStr);
				if( (hasJpf != null && hasJpf) || IoUtil.isResourceExists(manFile) )
				{
					ManifestInfo info = pluginManager.getRegistry().readManifestInfo(manFile);
					TLEPluginLocation location = new TLEPluginLocation(info, filename, version, context, manFile);
					TLEPluginLocation prevLocation = plugins.get(pluginId);
					if( prevLocation == null || prevLocation.getVersion() < version )
					{
						plugins.put(pluginId, location);
					}
					hasJpf = true;
				}
				else
				{
					hasJpf = false;
				}
				jarContainsPlugin.put(manFileStr, hasJpf);
			}
		}
	}

	public static String getMyPluginId(Class<?> callerClass)
	{
		return thisService.getPluginIdForObject(callerClass);
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
				catch( Exception e )
				{
					locator.setThrowable(e);
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

		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "LocatorPool-" + threadNumber.getAndIncrement());
		}
	}

}

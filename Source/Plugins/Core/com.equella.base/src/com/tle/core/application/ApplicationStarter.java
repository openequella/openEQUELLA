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

package com.tle.core.application;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ManifestInfo;

import com.google.common.collect.Sets;
import com.tle.core.application.impl.PluginServiceImpl;
import com.tle.core.plugins.AbstractPluginService.TLEPluginLocation;
import com.tle.core.plugins.PluginTracker;

@SuppressWarnings("nls")
public final class ApplicationStarter
{
	private static final Log LOGGER = LogFactory.getLog(ApplicationStarter.class);

	private static PluginTracker<StartupBean> startupTracker;

	private ApplicationStarter()
	{
		throw new Error();
	}

	public static void start(PluginManager pluginManager, Collection<Object[]> registeredAlready,
		Collection<String> types)
	{
		Set<TLEPluginLocation> registered = Sets.newHashSet();

		for( Object[] entry : registeredAlready )
		{
			registered.add(new TLEPluginLocation((ManifestInfo) entry[0], (String) entry[1], (Integer) entry[2],
				(URL) entry[3], (URL) entry[4]));
		}
		PluginServiceImpl pluginService = new PluginServiceImpl(registered);
		pluginService.setPluginManager(pluginManager);
		startupTracker = new PluginTracker<StartupBean>(pluginService, "com.tle.core.application", "onStartup", null,
			new PluginTracker.ExtensionParamComparator("order", true)).setBeanKey("bean");

		for( String type : types )
		{
			startRoles(type);
		}
	}

	private static final void startRoles(String type)
	{
		// This code has been vastly simplified.  
		// It used to use a thread pool to run "initial" tasks concurrently, but there is actually a defined order these things need to run in.
		List<Extension> startups = startupTracker.getExtensions();
		for( final Extension extension : startups )
		{
			final String extType = extension.getParameter("type").valueAsString();
			if( extType.equals(type) )
			{
				long start = System.currentTimeMillis();
				LOGGER.info("Starting bean " + extension);
				StartupBean startupBean = startupTracker.getBeanByExtension(extension);
				startupBean.startup();
				long end = System.currentTimeMillis();
				LOGGER.info("Startup bean " + extension + " took " + (end - start) + "ms.");
			}
		}
	}
}

/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.plugins;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;

import com.tle.common.filters.Filter;
import com.tle.core.plugins.AbstractPluginService.TLEPluginLocation;

public interface PluginService
{
	PluginBeanLocator getBeanLocator(String pluginId);

	PluginDescriptor getPluginDescriptor(String id);

	ExtensionPoint getExtensionPoint(String pluginId, String pointId);

	Collection<Extension> getConnectedExtensions(String pluginId, String pointId);

	Object getBean(String id, String clazzName);

	Object getBean(PluginDescriptor plugin, String clazzName);

	Iterable<URL> getLocalClassPath(String pluginId);

	ClassLoader getClassLoader(PluginDescriptor plugin);

	ClassLoader getClassLoader(String pluginId);

	void ensureActivated(PluginDescriptor plugin);

	void registerExtensionListener(String pluginId, String extensionId, RegistryChangeListener listener);

	String getPluginIdForObject(Object object);

	Plugin getPluginForObject(Object object);

	boolean isPluginDisabled(TLEPluginLocation location);

	Map<String, TLEPluginLocation> getPluginIdToLocation();

	Set<PluginDescriptor> getAllPluginsAndDependencies(Filter<PluginDescriptor> filter, Set<String> disallowed,
		boolean includeOptional);

	void initLocatorsFor(List<Extension> extensions);
}

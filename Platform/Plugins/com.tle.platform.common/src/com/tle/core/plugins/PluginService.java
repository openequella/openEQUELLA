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

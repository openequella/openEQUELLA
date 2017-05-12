package com.tle.core.plugins;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.ManifestInfo;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.PluginRegistry.RegistryChangeData;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;

import com.tle.client.PluginClassResolver;
import com.tle.common.filters.Filter;

public abstract class AbstractPluginService extends PluginClassResolver implements PluginService
{
	protected PluginManager pluginManager;
	protected static PluginService thisService;

	protected Map<String, TLEPluginLocation> pluginIdToLocation = new HashMap<String, TLEPluginLocation>();
	protected Map<String, List<RegistryChangeListener>> changeListeners = new HashMap<String, List<RegistryChangeListener>>();

	public void setPluginManager(PluginManager pluginManager)
	{
		this.pluginManager = pluginManager;
	}

	public AbstractPluginService()
	{
		thisService = this; // NOSONAR
	}

	@Override
	public Map<String, TLEPluginLocation> getPluginIdToLocation()
	{
		return pluginIdToLocation;
	}

	public static PluginService get()
	{
		return thisService;
	}

	public static String getMyPluginId(Class<?> callerClass)
	{
		return thisService.getPluginIdForObject(callerClass);
	}

	@Override
	public ClassLoader getLoaderForPluginId(String pluginId)
	{
		try
		{
			PluginDescriptor descriptor = getPluginDescriptor(pluginId);
			ensureActivated(descriptor);
			return getClassLoader(descriptor);
		}
		catch( IllegalArgumentException iae )
		{
			return getClass().getClassLoader();
		}
	}

	public static class TLEPluginLocation implements org.java.plugin.PluginManager.PluginLocation
	{
		private final ManifestInfo manifestInfo;
		private final String jar;
		private final URL context;
		private final URL manifest;
		private final int version;

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof TLEPluginLocation) )
			{
				return false;
			}

			return manifestInfo.getId().equals(((TLEPluginLocation) obj).manifestInfo.getId());
		}

		@Override
		public int hashCode()
		{
			return manifestInfo.getId().hashCode();
		}

		public TLEPluginLocation(ManifestInfo info, String jar, URL context, URL manifest)
		{
			this(info, jar, -1, context, manifest);
		}

		public TLEPluginLocation(ManifestInfo info, String jar, int version, URL context, URL manifest)
		{
			this.manifestInfo = info;
			this.jar = jar;
			this.context = context;
			this.manifest = manifest;
			this.version = version;
		}

		public int getVersion()
		{
			return version;
		}

		public String getJar()
		{
			return jar;
		}

		@Override
		public URL getContextLocation()
		{
			return context;
		}

		@Override
		public URL getManifestLocation()
		{
			return manifest;
		}

		public ManifestInfo getManifestInfo()
		{
			return manifestInfo;
		}
	}

	protected Object instantiatePluginClass(PluginDescriptor plugin, String clazz)
	{
		try
		{
			if( !pluginManager.isPluginActivated(plugin) )
			{
				pluginManager.activatePlugin(plugin.getId());
			}
			return pluginManager.getPluginClassLoader(plugin).loadClass(clazz).newInstance();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public ClassLoader getClassLoader(PluginDescriptor plugin)
	{
		return pluginManager.getPluginClassLoader(plugin);
	}

	@Override
	public Iterable<URL> getLocalClassPath(String pluginId)
	{
		PluginDescriptor descriptor = pluginManager.getRegistry().getPluginDescriptor(pluginId);
		Collection<Library> libraries = descriptor.getLibraries();
		ArrayList<URL> urls = new ArrayList<URL>();
		for( Library lib : libraries )
		{
			if( lib.isCodeLibrary() && !lib.getPath().endsWith(".jar") ) //$NON-NLS-1$
			{
				urls.add(pluginManager.getPathResolver().resolvePath(lib, lib.getPath()));
			}
		}
		return urls;
	}

	@Override
	public ClassLoader getClassLoader(String pluginId)
	{
		PluginDescriptor desc = pluginManager.getRegistry().getPluginDescriptor(pluginId);
		return getClassLoader(desc);
	}

	@Override
	public void ensureActivated(PluginDescriptor plugin)
	{
		try
		{
			pluginManager.activatePlugin(plugin.getId());
		}
		catch( PluginLifecycleException e )
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public ExtensionPoint getExtensionPoint(String pluginId, String pointId)
	{
		PluginDescriptor descId = pluginManager.getRegistry().getPluginDescriptor(pluginId);
		ExtensionPoint extPoint = descId.getExtensionPoint(pointId);
		if( extPoint == null )
		{
			throw new Error("ExtensionPoint " + pointId + " not found for plugin " + pluginId);
		}
		return extPoint;
	}

	@Override
	public Collection<Extension> getConnectedExtensions(String pluginId, String pointId)
	{
		return getExtensionPoint(pluginId, pointId).getConnectedExtensions();
	}

	@Override
	public Object getBean(String id, String clazzName)
	{
		return getBean(pluginManager.getRegistry().getPluginDescriptor(id), clazzName);
	}

	@Override
	public void registerExtensionListener(String pluginId, String extensionId, RegistryChangeListener listener)
	{
		String uniqueId = pluginManager.getRegistry().makeUniqueId(pluginId, extensionId);
		List<RegistryChangeListener> listenerList = changeListeners.get(uniqueId);
		if( listenerList == null )
		{
			listenerList = new ArrayList<RegistryChangeListener>();
		}
		listenerList.add(listener);
		changeListeners.put(uniqueId, listenerList);
	}

	@Override
	public String getPluginIdForObject(Object object)
	{
		Plugin plugin = pluginManager.getPluginFor(object);
		if( plugin != null )
		{
			return plugin.getDescriptor().getId();
		}
		return null;
	}

	@Override
	public Plugin getPluginForObject(Object object)
	{
		return pluginManager.getPluginFor(object);
	}

	public void registryChanged(RegistryChangeData changed)
	{
		for( String extpointId : changeListeners.keySet() )
		{
			if( !changed.modifiedExtensions(extpointId).isEmpty() || !changed.removedExtensions(extpointId).isEmpty()
				|| !changed.addedExtensions(extpointId).isEmpty() )
			{
				List<RegistryChangeListener> listenerList = changeListeners.get(extpointId);
				if( listenerList != null )
				{
					for( RegistryChangeListener listener : listenerList )
					{
						listener.registryChanged(changed);
					}
				}
			}
		}
	}

	@Override
	public Set<PluginDescriptor> getAllPluginsAndDependencies(Filter<PluginDescriptor> filter, Set<String> disallowed,
		boolean includeOptional)
	{
		PluginRegistry registry = pluginManager.getRegistry();

		Set<PluginDescriptor> rv = new HashSet<PluginDescriptor>();
		for( PluginDescriptor d : registry.getPluginDescriptors() )
		{
			if( !rv.contains(d) && (filter == null || filter.include(d)) )
			{
				rv.add(d);
				recursivelyIncludePrerequisites(registry, rv, d, new LinkedList<String>(), disallowed, includeOptional);
			}
		}
		return rv;
	}

	@SuppressWarnings("nls")
	private void recursivelyIncludePrerequisites(PluginRegistry registry, Set<PluginDescriptor> plugins,
		PluginDescriptor desc, Deque<String> trail, Set<String> disallowed, boolean includeOptional)
	{
		trail.push(desc.getId());
		for( PluginPrerequisite ppr : desc.getPrerequisites() )
		{
			PluginDescriptor pprDesc = registry.getPluginDescriptor(ppr.getPluginId());
			if( (includeOptional || !ppr.isOptional()) && !plugins.contains(pprDesc) )
			{
				if( disallowed.contains(pprDesc.getId()) )
				{
					throw new RuntimeException("Plugin " + desc.getId() + " requires banned plugin " + pprDesc.getId()
						+ ", trail is:" + trail);
				}
				plugins.add(pprDesc);
				recursivelyIncludePrerequisites(registry, plugins, pprDesc, trail, disallowed, includeOptional);
			}

		}
		trail.pop();
	}

	public PluginManager getPluginManager()
	{
		return pluginManager;
	}

	@Override
	public PluginDescriptor getPluginDescriptor(String id)
	{
		return pluginManager.getRegistry().getPluginDescriptor(id);
	}

	@Override
	public PluginBeanLocator getBeanLocator(String pluginId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void initLocatorsFor(List<Extension> extensions)
	{
		// do nothing
	}

	@Override
	public String getPluginIdForClass(Class<?> clazz)
	{
		return getPluginIdForObject(clazz);
	}
}

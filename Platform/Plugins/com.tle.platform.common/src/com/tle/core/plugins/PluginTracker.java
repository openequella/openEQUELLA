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

package com.tle.core.plugins;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ParameterType;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry.RegistryChangeData;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.tle.common.Check;
import com.tle.common.filters.Filter;

@SuppressWarnings("nls")
public class PluginTracker<T extends Object> implements RegistryChangeListener
{
	public static final String UNIQUE_ID_FOR_KEY = "$uniqueid$";
	public static final String LOCAL_ID_FOR_KEY = "$id$";

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginTracker.class);
	private final PluginService service;
	private final String pluginId;
	private final String extensionId;
	private final String parameterKey;
	private final Object cacheLock = new Object();

	private String beanKey;
	private Comparator<Extension> comparator;

	// Cacheable data
	private Map<String, Extension> map;
	private Map<String, T> beanMap;
	private List<Extension> allExtensions;
	private List<T> beanList;
	private Map<Extension, T> extBeanMap;

	public Map<String, T> getBeanMap()
	{
		synchronized( cacheLock )
		{
			if( beanMap == null )
			{
				beanMap = getNewBeanMap();
			}
			return beanMap;
		}
	}

	public Map<String, T> getNewBeanMap()
	{
		Map<String, T> newBeanMap = new LinkedHashMap<String, T>();
		service.initLocatorsFor(getExtensions());
		for( Map.Entry<String, Extension> entry : getExtensionMap().entrySet() )
		{
			Extension extension = entry.getValue();
			Parameter parameter = extension.getParameter(beanKey);
			if( parameter != null )
			{
				try
				{
					T bean = getBeanByParameter(extension, parameter);
					newBeanMap.put(entry.getKey(), bean);
				}
				catch( RuntimeException re )
				{
					LOGGER.error("Error creating a bean from extension: '" + extension + "' with parameter:"
						+ parameter);
					throw re;
				}
			}
		}
		return newBeanMap;
	}

	public Collection<T> getNewBeanList()
	{
		return getNewBeanMap().values();
	}

	public List<T> getBeanList()
	{
		synchronized( cacheLock )
		{
			if( beanList == null )
			{
				beanList = new ArrayList<T>();
				List<Extension> extensions = getExtensions();
				service.initLocatorsFor(extensions);
				for( Extension extension : extensions )
				{
					Collection<Parameter> parameters = extension.getParameters(beanKey);
					for( Parameter parameter : parameters )
					{
						try
						{
							T bean = getBeanByParameter(extension, parameter);
							beanList.add(bean);
						}
						catch( RuntimeException re )
						{
							LOGGER.error("Error creating a bean from extension: '" + extension + "' with parameter:"
								+ parameter);
						}
					}
				}
			}
			return beanList;
		}
	}


	public PluginTracker(PluginService service, String pluginId, String extensionId, String parameterKey)
	{
		this(service, pluginId, extensionId, parameterKey, null);
	}

	public PluginTracker(PluginService service, String pluginId, String extensionId, String parameterKey,
		Comparator<Extension> comparator)
	{
		this.service = service;
		this.pluginId = pluginId;
		this.extensionId = extensionId;
		this.parameterKey = parameterKey;
		this.comparator = comparator;
		service.registerExtensionListener(pluginId, extensionId, this);
	}

	@Override
	public void registryChanged(RegistryChangeData arg0)
	{
		synchronized( cacheLock )
		{
			map = null;
			beanMap = null;
			beanList = null;
			allExtensions = null;
			extBeanMap = null;
		}
	}

	public boolean needsUpdate()
	{
		synchronized( cacheLock )
		{
			return allExtensions == null;
		}
	}

	public Map<String, Extension> getExtensionMap()
	{
		synchronized( cacheLock )
		{
			if( map == null )
			{
				List<Extension> extensions = getExtensions();
				map = new LinkedHashMap<String, Extension>()
				{
					@Override
					public Extension put(String key, Extension value)
					{
						Extension rv = super.put(key, value);
						if( rv != null )
						{
							// This used to only log a warning but now throws an
							// exception as duplicate keys for an extension can
							// actually cause subtle issues that are really not
							// easily detected.
							throw new RuntimeException("Tracker '" + pluginId + ':' + extensionId + "' contains id:'"
								+ key + "' already");
						}
						return rv;
					}
				};

				for( Extension ext : extensions )
				{
					if( parameterKey == null || UNIQUE_ID_FOR_KEY.equals(parameterKey) )
					{
						map.put(ext.getUniqueId(), ext);
					}
					else if( LOCAL_ID_FOR_KEY.equals(parameterKey) )
					{
						map.put(ext.getId(), ext);
					}
					else
					{
						Collection<Parameter> parameters = ext.getParameters(parameterKey);
						for( Parameter parameter : parameters )
						{
							map.put(parameter.valueAsString(), ext);
						}
					}
				}
			}
			return map;
		}
	}

	public List<Extension> getExtensions()
	{
		synchronized( cacheLock )
		{
			if( allExtensions == null )
			{
				allExtensions = new ArrayList<Extension>(service.getConnectedExtensions(pluginId, extensionId));
				if( comparator != null )
				{
					Collections.sort(allExtensions, comparator);
				}
			}
			return allExtensions;
		}
	}

	public List<Extension> getExtensions(Filter<Extension> filter)
	{
		List<Extension> result = new ArrayList<Extension>();
		for( Extension ext : getExtensions() )
		{
			if( filter.include(ext) )
			{
				result.add(ext);
			}
		}
		return result;
	}

	public Extension getExtension(String key)
	{
		return key == null ? null : getExtensionMap().get(key);
	}

	public Extension getExtension(String key, String fallbackKey)
	{
		Extension ext = getExtension(key);
		if( ext == null )
		{
			ext = getExtension(fallbackKey);
		}
		return ext;
	}

	public T getNewBeanByExtension(Extension extension)
	{
		return getBeanByParameter(extension, beanKey);
	}

	public T getBeanByExtension(Extension extension)
	{
		T bean = null;
		synchronized( cacheLock )
		{
			if( extBeanMap != null )
			{
				bean = extBeanMap.get(extension);
			}
		}
		if( bean == null )
		{
			bean = getBeanByParameter(extension, beanKey);
			synchronized( cacheLock )
			{
				if( extBeanMap == null )
				{
					extBeanMap = new HashMap<Extension, T>();
				}
				extBeanMap.put(extension, bean);
			}
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	public T getBeanByParameter(Extension extension, Parameter parameter)
	{
		return (T) service.getBean(extension.getDeclaringPluginDescriptor(), parameter.valueAsString());
	}

	public T getBeanByParameter(Extension extension, String paramterName)
	{
		return getBeanByParameter(extension, extension.getParameter(paramterName));
	}

	public InputStream getResourceAsStream(Extension extension, String file)
	{
		ClassLoader classLoader = service.getClassLoader(extension.getDeclaringPluginDescriptor());
		return classLoader.getResourceAsStream(file);
	}

	public PluginTracker<T> setBeanKey(String beanKey)
	{
		this.beanKey = beanKey;
		return this;
	}

	public static class ExtensionParamComparator implements Comparator<Extension>, Serializable
	{
		private final String param;
		private final boolean nullsLast;
		private boolean haveType;
		private boolean number;
		private boolean date;

		public ExtensionParamComparator()
		{
			this("order", false);
		}

		public ExtensionParamComparator(String param)
		{
			this(param, false);
		}

		public ExtensionParamComparator(String param, boolean nullsLast)
		{
			this.param = param;
			this.nullsLast = nullsLast;
		}

		@Override
		public int compare(Extension ext1, Extension ext2)
		{
			Parameter param1 = ext1.getParameter(param);
			Parameter param2 = ext2.getParameter(param);
			if( param1 == param2 )
			{
				return 0;
			}
			if( param1 == null )
			{
				return nullsLast ? 1 : -1;
			}
			if( param2 == null )
			{
				return nullsLast ? -1 : 1;
			}
			if( !haveType )
			{
				determineType(param1);
			}
			if( number )
			{
				return (int) (param1.valueAsNumber().doubleValue() - param2.valueAsNumber().doubleValue());
			}
			if( date )
			{
				return param1.valueAsDate().compareTo(param2.valueAsDate());
			}
			return param1.valueAsString().compareTo(param2.valueAsString());
		}

		private void determineType(Parameter p)
		{
			ParameterType type = p.getDefinition().getType();

			if( type == ParameterType.NUMBER )
			{
				number = true;
			}
			else if( type == ParameterType.DATE )
			{
				date = true;
			}
			haveType = true;
		}
	}

	public static abstract class AbstractParamFilter implements Filter<Extension>
	{
		protected final String parameter;
		protected final boolean includeIfNoParam;

		public AbstractParamFilter(String parameter)
		{
			this(parameter, false);
		}

		public AbstractParamFilter(String parameter, boolean includeIfNoParam)
		{
			this.parameter = parameter;
			this.includeIfNoParam = includeIfNoParam;
		}

		@Override
		public boolean include(Extension ext)
		{
			Collection<Parameter> ps = ext.getParameters(parameter);
			if( ps.isEmpty() )
			{
				return includeIfNoParam;
			}

			for( Parameter param : ps )
			{
				if( compareParam(param) )
				{
					return true;
				}
			}

			return false;
		}

		protected abstract boolean compareParam(Parameter param);
	}

	public static class ContainsParamFilter extends AbstractParamFilter
	{
		private Set<String> values;

		public ContainsParamFilter(String parameter, Set<String> values)
		{
			super(parameter, false);
			this.values = values;
		}

		@Override
		protected boolean compareParam(Parameter param)
		{
			return values.contains(param.valueAsString());
		}
	}

	public static class NotContainsParamFilter implements Filter<Extension>
	{
		private final Set<String> values;
		private final String parameter;

		public NotContainsParamFilter(String parameter, Set<String> values)
		{
			this.parameter = parameter;
			this.values = values;
		}

		@Override
		public boolean include(Extension ext)
		{
			Collection<Parameter> ps = ext.getParameters(parameter);
			for( Parameter param : ps )
			{
				if( values.contains(param.valueAsString()) )
				{
					return false;
				}
			}
			return true;
		}
	}

	public static class ParamFilter extends AbstractParamFilter
	{
		protected Set<String> values;

		public ParamFilter(String parameter, String... values)
		{
			this(parameter, false, values);
		}

		public ParamFilter(String parameter, boolean includeIfNoParam, String... values)
		{
			super(parameter, includeIfNoParam);
			this.values = Sets.newHashSet(values);
		}

		@Override
		protected boolean compareParam(Parameter param)
		{
			for( String value : values )
			{
				if( Check.bothNullOrEqual(param.valueAsString(), value) )
				{
					return true;
				}
			}
			return false;
		}
	}

	public Class<?> getClassForName(Extension extension, String clazzName)
	{
		try
		{
			PluginDescriptor descriptor = extension.getDeclaringPluginDescriptor();
			service.ensureActivated(descriptor);
			return service.getClassLoader(descriptor).loadClass(clazzName);
		}
		catch( ClassNotFoundException e )
		{
			throw new RuntimeException(e);
		}
	}

	public PluginService getService()
	{
		return service;
	}

	public boolean isParamTrue(Extension extension, String paramName, boolean defaultValue)
	{
		Parameter param = extension.getParameter(paramName);
		if( param == null )
		{
			return defaultValue;
		}
		return param.valueAsBoolean();
	}
}

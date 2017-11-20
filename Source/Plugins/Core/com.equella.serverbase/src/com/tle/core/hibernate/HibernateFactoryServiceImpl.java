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

package com.tle.core.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.standard.DynamicClassLoader;
import org.java.plugin.standard.StandardPluginClassLoader;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

@Bind(HibernateFactoryService.class)
@Singleton
public class HibernateFactoryServiceImpl implements HibernateFactoryService, DynamicClassLoader
{
	private static final String EXTPOINT_ID = "domainObjects"; //$NON-NLS-1$
	private static final String MAIN_FACTORY = "main"; //$NON-NLS-1$

	private PluginTracker<Object> domainTracker;
	private Map<String, List<Extension>> extensionMap;
	private Map<String, Class<?>> accessibleClasses = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		domainTracker = new PluginTracker<Object>(pluginService, "com.tle.core.hibernate", EXTPOINT_ID, null); // NOSONAR
		((StandardPluginClassLoader) SessionFactory.class.getClassLoader()).setDynamicClassLoader(this);
	}

	@Override
	public HibernateFactory createConfiguration(DataSourceHolder dataSourceHolder, Class<?>... clazzes)
	{
		for( Class<?> clazz : clazzes )
		{
			accessibleClasses.put(clazz.getName(), clazz);
		}
		return new HibernateFactory(dataSourceHolder, clazzes);
	}

	@Override
	public synchronized Class<?>[] getDomainClasses(String factory)
	{
		if( extensionMap == null || domainTracker.needsUpdate() )
		{
			extensionMap = new HashMap<String, List<Extension>>();
			List<Extension> extensions = domainTracker.getExtensions();
			for( Extension extension : extensions )
			{
				String name = MAIN_FACTORY;

				Parameter factoryParam = extension.getParameter("factory"); //$NON-NLS-1$
				if( factoryParam != null )
				{
					name = factoryParam.valueAsString();
				}
				List<Extension> list = extensionMap.get(name);
				if( list == null )
				{
					list = new ArrayList<Extension>();
					extensionMap.put(name, list);
				}
				list.add(extension);
			}
		}
		List<Class<?>> clazzes = new ArrayList<Class<?>>();
		List<Extension> extlist = extensionMap.get(factory);
		if( extlist != null )
		{
			for( Extension extension : extlist )
			{
				Collection<Parameter> params = extension.getParameters("class"); //$NON-NLS-1$
				for( Parameter cp : params )
				{
					Class<?> clazz = domainTracker.getClassForName(extension, cp.valueAsString());
					clazzes.add(clazz);
				}
			}
		}
		return clazzes.toArray(new Class<?>[clazzes.size()]);
	}

	@Override
	public Class<?> findClass(String name)
	{
		return accessibleClasses.get(name);
	}
}

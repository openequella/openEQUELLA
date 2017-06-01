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

package com.tle.web.freemarker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;

import freemarker.cache.TemplateLoader;

@Bind
@Singleton
public class CustomTemplateLoader implements TemplateLoader
{
	private static Log LOGGER = LogFactory.getLog(CustomTemplateLoader.class);

	@Inject
	private PluginService pluginService;
	private final Map<String, PluginFreemarkerFactory> factories = new HashMap<String, PluginFreemarkerFactory>();

	public PluginService getPluginService()
	{
		return pluginService;
	}

	@Override
	public void closeTemplateSource(Object source) throws IOException
	{
		((URLTemplateSource) source).close();
	}

	@SuppressWarnings("nls")
	@Override
	public Object findTemplateSource(String template) throws IOException
	{
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Looking for " + template);
		}
		int ind = template.indexOf('/');
		String factoryName = template.substring(0, ind);
		template = template.substring(ind + 1);
		PluginFreemarkerFactory factory = factories.get(factoryName);
		if( factory == null )
		{
			LOGGER.error("No factory for :'" + factoryName + "' path:'" + template);
			return null;
		}
		URL urlFor = factory.getTemplateResource(template);
		return new URLTemplateSource(urlFor);
	}

	@Override
	public long getLastModified(Object source)
	{
		return ((URLTemplateSource) source).lastModified();
	}

	@Override
	public Reader getReader(Object source, String charset) throws IOException
	{
		return new InputStreamReader(((URLTemplateSource) source).getInputStream(), charset);
	}

	@SuppressWarnings("nls")
	public synchronized void addFactory(String id, PluginFreemarkerFactory factory)
	{
		LOGGER.debug("Adding factory " + id);
		if( !factories.containsKey(id) )
		{
			factories.put(id, factory);
		}
		else
		{
			throw new Error("Already have factory for '" + id + "'. Please name your factories.");
		}
	}

	public synchronized PluginFreemarkerFactory getFactoryForName(String factoryName)
	{
		return factories.get(factoryName);
	}
}

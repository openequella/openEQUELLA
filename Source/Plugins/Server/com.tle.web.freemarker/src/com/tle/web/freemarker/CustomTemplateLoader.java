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

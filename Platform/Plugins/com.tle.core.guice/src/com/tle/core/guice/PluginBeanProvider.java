package com.tle.core.guice;

import javax.inject.Inject;

import org.java.plugin.registry.PluginDescriptor;

import com.google.inject.Provider;
import com.tle.core.plugins.PluginService;

public class PluginBeanProvider<T> implements Provider<T>
{
	@Inject
	private PluginService pluginService;
	private String beanName;
	private Object pluginObject;

	public PluginBeanProvider(Object pluginObject, String beanName)
	{
		this.pluginObject = pluginObject;
		this.beanName = beanName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get()
	{
		PluginDescriptor descriptor;
		if( pluginObject instanceof String )
		{
			descriptor = pluginService.getPluginDescriptor((String) pluginObject);
		}
		else
		{
			descriptor = pluginService.getPluginForObject(pluginObject).getDescriptor();
		}
		return (T) pluginService.getBean(descriptor, beanName);
	}

}

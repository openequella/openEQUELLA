package com.tle.client;

public class PluginClassResolver
{
	static PluginClassResolver instance;

	public PluginClassResolver()
	{
		instance = this; // NOSONAR
	}

	public static PluginClassResolver resolver()
	{
		if( instance == null )
		{
			instance = new PluginClassResolver();
		}
		return instance;
	}

	public ClassLoader getLoaderForPluginId(String pluginId)
	{
		return getClass().getClassLoader();
	}

	public String getPluginIdForClass(Class<?> clazz)
	{
		return null;
	}

}

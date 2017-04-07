package com.tle.core.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;

import com.tle.client.PluginClassResolver;

public class PluginAwareObjectInputStream extends ObjectInputStream
{
	private List<ClassLoader> loaders = new ArrayList<ClassLoader>();

	public PluginAwareObjectInputStream(InputStream stream) throws IOException
	{
		super(stream);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	{
		int loaderNum = readByte();
		ClassLoader loader;
		if( loaderNum == 0 )
		{
			loader = Thread.currentThread().getContextClassLoader();
		}
		else if( loaderNum > loaders.size() )
		{
			String pluginId = readUTF();
			PluginClassResolver pluginService = PluginClassResolver.resolver();
			loader = pluginService.getLoaderForPluginId(pluginId);
			loaders.add(loader);
		}
		else
		{
			loader = loaders.get(loaderNum - 1);
		}
		try
		{
			return Class.forName(desc.getName(), false, loader);
		}
		catch( ClassNotFoundException cnfe )
		{
			return super.resolveClass(desc);
		}
	}

	public static Object fromBytes(byte[] bytes)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try
		{
			PluginAwareObjectInputStream paois = new PluginAwareObjectInputStream(bais);
			Object obj = paois.readObject();
			paois.close();
			return obj;
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
		catch( ClassNotFoundException e )
		{
			throw new RuntimeException(e);
		}
	}
}
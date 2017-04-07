package com.tle.core.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.tle.client.PluginClassResolver;

public class PluginAwareObjectOutputStream extends ObjectOutputStream
{
	private Map<String, Integer> pluginClassLoaders = new HashMap<String, Integer>();

	public PluginAwareObjectOutputStream(OutputStream out) throws IOException
	{
		super(out);
	}

	@Override
	protected void annotateClass(Class<?> cl) throws IOException
	{
		String pluginId = PluginClassResolver.resolver().getPluginIdForClass(cl);
		if( pluginId != null )
		{
			Integer offset = pluginClassLoaders.get(pluginId);
			if( offset == null )
			{
				offset = pluginClassLoaders.size() + 1;
				writeByte(offset);
				writeUTF(pluginId);
				pluginClassLoaders.put(pluginId, offset);
			}
			else
			{
				writeByte(offset);
			}
		}
		else
		{
			writeByte(0);
		}
	}

	public static byte[] toBytes(Object object)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			PluginAwareObjectOutputStream paoos = new PluginAwareObjectOutputStream(baos);
			paoos.writeObject(object);
			paoos.close();
			return baos.toByteArray();
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

}

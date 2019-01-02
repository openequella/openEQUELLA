/*
 * Copyright 2019 Apereo
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

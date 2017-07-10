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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.client.PluginClassResolver;

public class PluginAwareObjectInputStream extends ObjectInputStream
{
	private List<ClassLoader> loaders = new ArrayList<ClassLoader>();
	private Set<String> banned = Sets.newHashSet(
			"org.apache.commons.collections.functors.InvokerTransformer", "org.apache.commons.collections4.functors.InvokerTransformer",
		"org.apache.commons.collections.functors.InstantiateTransformer",
		"org.apache.commons.collections4.functors.InstantiateTransformer",
		"org.codehaus.groovy.runtime.ConvertedClosure",
		"org.codehaus.groovy.runtime.MethodClosure",
		"org.springframework.beans.factory.ObjectFactory",
		"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"
	);

	public PluginAwareObjectInputStream(InputStream stream) throws IOException
	{
		super(stream);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	{
		if (banned.contains(desc.getName()))
		{
			throw new RuntimeException("Class is banned: "+desc.getName());
		}
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
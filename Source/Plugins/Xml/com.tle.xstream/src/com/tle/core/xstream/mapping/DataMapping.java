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

package com.tle.core.xstream.mapping;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.XMLDataChild;
import com.tle.core.xstream.XMLDataResolver;

/**
 * 
 */
public class DataMapping extends AbstractMapping
{
	Class clazz;
	private XMLDataResolver resolver;

	public DataMapping(String name, String node, Class clazz)
	{
		this(name, node, clazz, null);
	}

	public DataMapping(String name, String node, Class clazz, XMLDataResolver resolver)
	{
		super(name, node);
		this.clazz = clazz;
		setClassResolver(resolver);
	}

	public void setClassResolver(XMLDataResolver resolver)
	{
		if( resolver == null )
		{
			resolver = new DefaultDataResolver();
		}
		this.resolver = resolver;
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		if( name.length() > 0 )
		{
			object = getField(object);
		}

		resolver.writeClass(writer, object);
		context.convertAnother(object);
	}

	@Override
	public boolean hasValue(Object object)
	{
		return getField(object) != null;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Class clazz2use = resolver.resolveClass(reader);
		if( clazz2use == null )
		{
			clazz2use = clazz;
		}
		Object current = null;

		// Collections Data test
		if( name.length() > 0 )
		{
			// Allows fields to be pre-initialised
			current = getField(object);
		}
		if( current == null )
		{
			current = object;
		}

		Object retobj = context.convertAnother(current, clazz2use);
		if( retobj instanceof XMLDataChild )
		{
			((XMLDataChild) retobj).setParentObject(object);
		}
		return retobj;
	}

	public class DefaultDataResolver implements XMLDataResolver
	{
		private static final String XCLASS = "xclass";

		@Override
		public Class resolveClass(HierarchicalStreamReader reader)
		{
			String xclass = reader.getAttribute(XCLASS);
			Class clazz2use = null;
			if( xclass != null )
			{
				try
				{
					clazz2use = Class.forName(xclass, false, Thread.currentThread().getContextClassLoader());
				}
				catch( ClassNotFoundException e )
				{
					throw new RuntimeException(e);
				}
			}
			return clazz2use;
		}

		@Override
		public void writeClass(HierarchicalStreamWriter writer, Object object)
		{
			if( object.getClass() != clazz )
			{
				writer.addAttribute(XCLASS, object.getClass().getName());
			}
		}
	}
}

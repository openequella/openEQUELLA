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

import java.io.Serializable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.ReflectionProvider;
import com.tle.core.xstream.XMLPath;

/**
 *
 */
public abstract class AbstractMapping implements Comparable<AbstractMapping>, Serializable
{
	private static final long serialVersionUID = 1;

	private static final ReflectionProvider REFLECTION = new ReflectionProvider();

	protected XMLPath nodePath;
	protected final String name;

	public AbstractMapping(String name, String node)
	{
		this.name = name;
		setXpath(node);
	}

	protected void setXpath(String xpath)
	{
		nodePath = new XMLPath(xpath);
	}

	public abstract void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object);

	public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
	{
		Object value = getUnmarshalledValue(object, reader, context);
		write(object, value);
	}

	protected void write(Object object, Object value)
	{
		if( !nodePath.hasAttribute() || value != null )
		{
			REFLECTION.writeField(object, name, value);
		}
	}

	protected boolean isBlank(String value)
	{
		return value != null && value.length() == 0;
	}

	protected abstract Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader,
		UnmarshallingContext context);

	public abstract boolean hasValue(Object object);

	protected Object getField(Object object)
	{
		return REFLECTION.getField(object, name);
	}

	protected void writeField(Object object, Object value)
	{
		REFLECTION.writeField(object, name, value);
	}

	protected Object newInstance(Class<?> clazz)
	{
		return REFLECTION.newInstance(clazz);
	}

	public XMLPath getNodePath()
	{
		return nodePath;
	}

	public String getFieldName()
	{
		return name;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return "{" + name + ":" + nodePath + "}";
	}

	@Override
	public int compareTo(AbstractMapping m1)
	{
		int compare = -1;
		if( m1.getNodePath().hasAttribute() )
		{
			compare = 1;
		}
		return compare;
	}
}

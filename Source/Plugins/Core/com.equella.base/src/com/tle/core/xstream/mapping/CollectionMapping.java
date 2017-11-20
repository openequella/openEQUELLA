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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
@SuppressWarnings("nls")
public class CollectionMapping extends AbstractTypeMapping
{
	private final AbstractMapping converter;

	private String endNode;

	public CollectionMapping(String name, String node)
	{
		this(name, node, null);
	}

	public CollectionMapping(String name, String node, Class<?> type)
	{
		this(name, node, type, new NodeMapping("", node));
	}

	public CollectionMapping(String name, String node, Class<?> coltype, Class<?> eltype)
	{
		this(name, node, coltype, new DataMapping("", node, eltype));
	}

	public CollectionMapping(String name, String node, Class<?> type, AbstractMapping converter)
	{
		super(name, node, type);
		this.converter = converter;
	}

	/**
	 * This is so we can deal with the last node specifically.
	 */
	@Override
	protected void setXpath(String xpath)
	{
		super.setXpath(xpath);
		endNode = nodePath.getLastNode();
	}

	// The intent presumably is to return the implementation class, so we ignore
	// the 'loose coupling' Sonar warning.
	@Override
	public Class<?> getDefaultType()
	{
		return ArrayList.class; // NOSONAR
	}

	@Override
	public Class<?> getRequiredType()
	{
		return Collection.class;
	}

	@Override
	public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
	{
		Collection<Object> col = getInstantiatedCollection(object);
		Object value = getUnmarshalledValue(object, reader, context);
		if( value != null )
		{
			col.add(value);
		}
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		return converter.getUnmarshalledValue(object, reader, context);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Collection<?> col = getCollection(object);
		if( col != null )
		{
			Iterator<?> i = col.iterator();
			boolean first = true;
			while( i.hasNext() )
			{
				Object value = i.next();
				if( value == null )
				{
					continue;
				}

				if( !first )
				{
					writer.endNode();
					writer.startNode(endNode);
				}
				first = false;
				converter.marshal(writer, context, value);
			}
		}
	}

	private Collection<?> getCollection(Object object)
	{
		return (Collection<?>) getField(object);
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> getInstantiatedCollection(Object object)
	{
		return (Collection<Object>) getInstantiatedField(object);
	}

	@Override
	public boolean hasValue(Object object)
	{
		Collection<?> col = getCollection(object);
		return col != null && col.size() > 0;
	}
}

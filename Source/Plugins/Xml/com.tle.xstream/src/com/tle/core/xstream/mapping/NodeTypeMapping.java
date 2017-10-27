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

import com.google.common.collect.BiMap;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * 
 */
public class NodeTypeMapping extends NodeMapping
{
	private BiMap<String, Integer> types;
	private Object defaultType;

	public NodeTypeMapping(String name, String node, BiMap<String, Integer> types)
	{
		this(name, node, types, null);
	}

	/**
	 * @param types - node value -> Object value. ie admin -> new Integer(5),
	 *            metadata -> new Integer(4), teacher -> new Integer(3),
	 */
	public NodeTypeMapping(String name, String node, BiMap<String, Integer> types, Object defaultType)
	{
		super(name, node);
		this.types = types;
		this.defaultType = defaultType;
	}

	@Override
	protected Object getMarshalledValue(Object object)
	{
		Object value = super.getMarshalledValue(object);

		if( value != null )
		{
			value = types.inverse().get(value);
		}
		else
		{
			value = types.inverse().get(defaultType);
		}
		return value;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Object value = super.getUnmarshalledValue(object, reader, context);
		value = types.get(value);
		if( value == null )
		{
			value = defaultType;
		}
		return value;
	}
}

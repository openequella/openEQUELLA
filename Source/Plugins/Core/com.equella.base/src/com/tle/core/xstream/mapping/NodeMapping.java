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

/**
 * 
 */
public class NodeMapping extends AbstractMapping
{
	public NodeMapping(String name, String node)
	{
		super(name, node);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Object value = getMarshalledValue(object);
		if( value != null )
		{
			String string = value.toString();
			nodePath.setValue(writer, string);
		}
	}

	protected Object getMarshalledValue(Object object)
	{
		Object value = null;

		// Special case for collections
		if( name.length() > 0 )
		{
			value = getField(object);
		}
		else
		{
			value = object;
		}
		return value;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		return nodePath.getValue(reader);
	}

	@Override
	public boolean hasValue(Object object)
	{
		return getMarshalledValue(object) != null;
	}
}

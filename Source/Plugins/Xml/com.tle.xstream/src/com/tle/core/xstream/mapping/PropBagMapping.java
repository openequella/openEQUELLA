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

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class PropBagMapping extends ElementMapping
{

	public PropBagMapping(String name, String node)
	{
		this(name, node, false);
	}

	public PropBagMapping(String name, String node, boolean useroot)
	{
		super(name, node, useroot);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		PropBagEx node = (PropBagEx) getMarshalledValue(object);
		super.marshalNode(writer, context, node.getRootElement());
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Node element = (Node) super.getUnmarshalledValue(object, reader, context);
		PropBagEx xml = null;
		if( element != null )
		{
			xml = new PropBagEx(element);
		}
		return xml;
	}
}

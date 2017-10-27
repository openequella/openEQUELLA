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

package com.tle.core.xstream;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.mapping.AbstractMapping;
import com.tle.core.xstream.mapping.ElementMapping;

/**
 * Used in conjunction with XStream to create a converter that can (hopefully)
 * marshal/unmarshal beans with a greater ease that setting one up manually.
 * 
 * @author Charles O'Farrell
 */
public class XMLDataConverter implements Converter
{
	private static final String SLASH = "/";
	private static final ReflectionProvider REFLECTION = new ReflectionProvider();

	@Override
	public boolean canConvert(Class kclass)
	{
		return XMLData.class.isAssignableFrom(kclass);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		XMLDataMappings mappings;
		synchronized( XMLDataConverter.class )
		{
			mappings = ((XMLData) source).getMappings();
		}
		marshal(source, mappings, writer, context);
	}

	public void marshal(Object source, XMLDataMappings mappings, HierarchicalStreamWriter writer,
		MarshallingContext context)
	{
		SortedMap<String, SortedSet<AbstractMapping>> nodeMapping = mappings.getMappings();
		Iterator<String> i = nodeMapping.keySet().iterator();
		Stack<String> current = new Stack<String>();

		while( i.hasNext() )
		{
			String node = i.next();
			Collection<AbstractMapping> col = getMapping(nodeMapping, node);
			Iterator j = col.iterator();
			boolean map = false;
			while( j.hasNext() )
			{
				AbstractMapping nodeMap = (AbstractMapping) j.next();
				map |= nodeMap.hasValue(source);
			}
			if( map )
			{
				String[] nodes = null;
				if( node.length() == 0 )
				{
					nodes = new String[]{};
				}
				else
				{
					nodes = node.substring(1).split(SLASH);
				}

				endNode(writer, current, nodes);
				startNode(writer, current, nodes);
				j = col.iterator();
				while( j.hasNext() )
				{
					AbstractMapping nodeMap = (AbstractMapping) j.next();
					nodeMap.marshal(writer, context, source);
				}
			}
		}

		while( current.size() > 0 )
		{
			current.pop();
			writer.endNode();
		}
	}

	private void startNode(HierarchicalStreamWriter writer, Stack<String> current, String[] nodes)
	{
		for( int i = 0; i < nodes.length; i++ )
		{
			String node = nodes[i];
			if( !(current.size() > i && node.equals(current.get(i))) )
			{
				current.push(node);
				writer.startNode(node);
			}
		}
	}

	private void endNode(HierarchicalStreamWriter writer, Stack current, String[] nodes)
	{
		int i = current.size() - 1;
		boolean isWithin = nodes.length > 0 && current.size() > 0 && current.get(0).toString().equals(nodes[0]);

		for( ; current.size() > 0 && i >= 0; i-- )
		{
			String node = current.peek().toString();
			if( ((isWithin && nodes.length > i && !node.equals(nodes[i])) || !isWithin) )
			{
				current.pop();
				writer.endNode();
			}
		}
	}

	public Object createDefaultInstance(Class type)
	{
		return REFLECTION.newInstance(type);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Object object = context.currentObject();
		if( object == null )
		{
			object = createDefaultInstance(context.getRequiredType());
		}

		XMLDataMappings mappings;
		synchronized( XMLDataConverter.class )
		{
			mappings = ((XMLData) object).getMappings();
		}
		recurseUnmarshal(object, reader, context, "", mappings);
		return object;
	}

	public void recurseUnmarshal(Object object, HierarchicalStreamReader reader, UnmarshallingContext context,
		String path, XMLDataMappings mappings)
	{
		Collection<AbstractMapping> col = getMapping(mappings.getMappings(), path);
		for( AbstractMapping nodeMap : col )
		{
			nodeMap.unmarshal(reader, context, object);
			if( nodeMap instanceof ElementMapping )
			{
				return;
			}
		}

		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String thispath = path + SLASH + processNodeName(mappings, reader.getNodeName());
			recurseUnmarshal(object, reader, context, thispath, mappings);
		}

	}

	private String processNodeName(XMLDataMappings mappings, String name)
	{
		if( mappings.isIgnoreNS() )
		{
			int index = name.indexOf(':');
			if( index > 0 )
			{
				name = name.substring(index + 1);
			}
		}
		return name;
	}

	public Collection<AbstractMapping> getMapping(SortedMap<String, SortedSet<AbstractMapping>> nodeMapping,
		String path)
	{
		Collection<AbstractMapping> col = nodeMapping.get(path);
		if( col == null )
		{
			col = Collections.emptyList();
		}
		return col;
	}
}

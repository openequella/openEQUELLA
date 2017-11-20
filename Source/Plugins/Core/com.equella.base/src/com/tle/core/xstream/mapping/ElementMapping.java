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

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class ElementMapping extends NodeMapping
{
	private static DocumentBuilderFactory factory;

	static
	{
		if( factory == null )
		{
			factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
		}
	}

	private boolean useroot;

	public ElementMapping(String name, String node)
	{
		this(name, node, false);
	}

	public ElementMapping(String name, String node, boolean useroot)
	{
		super(name, node);
		this.useroot = useroot;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().newDocument();
		}
		catch( ParserConfigurationException e )
		{
			throw new RuntimeException(e);
		}

		try
		{
			Element e = parseSubTree2(reader, doc);
			doc.appendChild(e);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		Element root = doc.getDocumentElement();
		if( useroot )
		{
			root = (Element) new PropBagEx(root).findNode("*");
		}
		return root;
	}

	private Element parseSubTree2(HierarchicalStreamReader reader, Document doc)
	{
		String qname = reader.getNodeName();
		Element parent = doc.createElement(qname);

		Iterator i = reader.getAttributeNames();
		while( i.hasNext() )
		{
			String aname = i.next().toString();
			String value = reader.getAttribute(aname);
			parent.setAttribute(aname, value);
		}

		String text = reader.getValue();
		if( text.trim().length() != 0 )
		{
			Text textEl = doc.createTextNode(text);
			parent.appendChild(textEl);
		}

		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			Element child = parseSubTree2(reader, doc);
			parent.appendChild(child);
		}

		return parent;
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Node node = (Node) getMarshalledValue(object);
		marshalNode(writer, context, node);
	}

	protected void marshalNode(HierarchicalStreamWriter writer, MarshallingContext context, Node node)
	{
		if( useroot )
		{
			writer.startNode(node.getNodeName());
		}
		rmarshalNode(writer, context, node);
		if( useroot )
		{
			writer.endNode();
		}
	}

	private void rmarshalNode(HierarchicalStreamWriter writer, MarshallingContext context, Node node)
	{
		NamedNodeMap map = node.getAttributes();
		int count;
		if( map != null )
		{
			count = map.getLength();
			for( int i = 0; i < count; i++ )
			{
				Node attribute = map.item(i);
				writer.addAttribute(attribute.getNodeName(), attribute.getNodeValue());
			}
		}
		NodeList list = node.getChildNodes();
		if( list != null )
		{
			count = list.getLength();
			for( int i = 0; i < count; i++ )
			{
				Node child = list.item(i);
				if( child.getNodeType() == Node.TEXT_NODE )
				{
					String value = child.getNodeValue();
					if( value != null && value.length() > 0 )
					{
						writer.setValue(value);
					}
				}
				else
				{
					writer.startNode(child.getNodeName());
					rmarshalNode(writer, context, child);
					writer.endNode();
				}
			}
		}
	}
}

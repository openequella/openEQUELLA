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

package com.tle.core.harvester.oai.xstream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.DublinCore;

/**
 * 
 */
public class OAIDOMConverter extends OAIAbstractConverter
{

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(DublinCore.class);
	}

	@Override
	public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		Node node = (Node) arg0;
		writer.startNode(node.getNodeName());
		rmarshalNode(writer, context, node);
		writer.endNode();
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

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		DocumentBuilderFactory factory;

		factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);

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
		return root; // NOSONAR (kept local variable for readability)

	}

	private Element parseSubTree2(HierarchicalStreamReader reader, Document doc)
	{
		String qname = reader.getNodeName();
		Element parent = doc.createElement(qname);

		// Iterator i = reader.getAttributeNames();
		int count = reader.getAttributeCount();
		for( int i = 0; i < count; i++ )
		{
			String aname = reader.getAttributeName(i);
			String value = reader.getAttribute(i);
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
}

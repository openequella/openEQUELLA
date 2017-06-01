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

package com.tle.core.harvester.old.dsoap.sax;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import com.tle.core.harvester.old.dsoap.SoapCallException;

/**
 * @author adame
 */
public class ElementArrayResultSoapHandler extends DefaultSoapHandler
{
	private List results;
	private Document doc;
	private Element root;
	private Element current;

	public ElementArrayResultSoapHandler() throws SoapCallException
	{
		results = new ArrayList();
		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch( Exception ex )
		{
			throw new SoapCallException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.soap.sax.DefaultSoapHandler#hookStartElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	protected void hookStartElement(String namespaceURL, String localName, String qname, Attributes attributes)
	{
		if( getDepth() >= 5 )
		{
			Element newElement = doc.createElementNS(namespaceURL, localName);

			int count = attributes.getLength();
			for( int i = 0; i < count; i++ )
			{
				newElement.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
			}

			if( getDepth() == 5 )
			{
				root = newElement;
				results.add(root);
			}
			else
			{
				current.appendChild(newElement);
			}
			current = newElement;
		}
	}

	@Override
	protected void hookEndElement(String namespaceURL, String localName, String qname)
	{
		if( getDepth() > 5 )
		{
			if( !current.hasChildNodes() )
			{
				current.appendChild(doc.createTextNode(getAcculumulator()));
			}
			current = (Element) current.getParentNode();
		}
		else if( getDepth() == 5 )
		{
			if( !root.equals(current) )
			{
				throw new RuntimeException("root should equal the current node's parent");
			}
		}
	}

	public Element[] getElementArrayResult()
	{
		int size = results.size();
		Element[] rv = new Element[size];
		for( int i = 0; i < size; ++i )
		{
			rv[i] = (Element) results.get(i);
		}
		return rv;
	}
}
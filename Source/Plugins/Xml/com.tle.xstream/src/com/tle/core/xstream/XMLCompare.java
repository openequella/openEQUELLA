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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class XMLCompare
{
	private static final Logger LOGGER = Logger.getLogger(XMLCompare.class.toString());
	/**
	 * A shared instance of the document builder factory.
	 */
	private static DocumentBuilderFactory documentFactory;

	/**
	 * Trim text elements before comparison.
	 */
	private boolean trimTextValues = false;

	/**
	 * Compares by DOM. Ordering requirements will be respected.
	 */
	public boolean compare(Reader reader1, Reader reader2) throws SAXException, IOException,
		ParserConfigurationException
	{
		return compare(getDocument(reader1), getDocument(reader2));
	}

	/**
	 * Compares by DOM. Ordering requirements will be respected.
	 */
	boolean compare(Document document1, Document document2)
	{
		// Make sure adjacent text nodes are combined
		document1.normalize();
		document2.normalize();

		return compareChildren(document1.getDocumentElement(), document2.getDocumentElement());
	}

	/**
	 * Checks common stuff about the children, and then dispatches to an
	 * implementation that respects the ordering requirements.
	 */
	private boolean compareChildren(Node n1, Node n2)
	{
		NodeList children1 = n1.getChildNodes();
		NodeList children2 = n2.getChildNodes();

		if( children1.getLength() != children2.getLength() )
		{
			if( sizeWithoutEmptyText(children1) != sizeWithoutEmptyText(children2) )
			{
				LOGGER.fine("Number of children for " + n1.getNodeName() + " not equivalent");
				return false;
			}
		}
		return compareChildrenUnordered(n1, n2);
	}

	/**
	 * Compares children which can be in any order.
	 */
	private boolean compareChildrenUnordered(Node n1, Node n2)
	{
		NodeList children1 = n1.getChildNodes();
		NodeList children2 = n2.getChildNodes();

		for( int i = 0; i < children1.getLength(); i++ )
		{
			Node child1 = children1.item(i);
			List<Node> possibleMatches = new ArrayList<Node>();
			boolean matchFoundExceptAttributes = false;
			boolean matchFoundExceptValue = false;

			for( int j = 0; j < children2.getLength(); j++ )
			{
				Node child2 = children2.item(j);
				if( equalNames(child1, child2) )
				{
					if( equalAttibutes(child1, child2) )
					{
						if( equalValues(child1, child2) )
						{
							possibleMatches.add(child2);
						}
						else
						{
							matchFoundExceptValue = true;
						}
					}
					else
					{
						matchFoundExceptAttributes = true;
					}
				}
			}

			boolean matchFound = false;
			Iterator<Node> iter = possibleMatches.iterator();
			while( !matchFound && iter.hasNext() )
			{
				Node child2 = iter.next();
				matchFound = compareChildren(child1, child2);
			}

			if( !matchFound )
			{
				LOGGER.fine("No equivalent child named " + child1.getNodeName() + " for " + n1.getNodeName());

				if( matchFoundExceptAttributes )
				{
					LOGGER.fine("A possible match was found, but attributes were not equivalent");
				}

				if( matchFoundExceptValue )
				{
					LOGGER.fine("A possible match was found, but values were not equivalent for '"
						+ child1.getNodeValue() + "'");
				}

				return false;
			}
		}

		return true;
	}

	/**
	 * Check if the names of two nodes are the same.
	 */
	private boolean equalNames(Node n1, Node n2)
	{
		return checkEqual(n1.getLocalName(), n2.getLocalName());
	}

	/**
	 * Check if the values of two nodes are the same.
	 */
	private boolean equalValues(Node n1, Node n2)
	{
		String v1 = n1.getNodeValue();
		String v2 = n2.getNodeValue();

		// In this case literal identity of 2 string objects is the intent
		if( v1 == v2 ) // NOSONAR
		{
			return true;
		}

		if( v1 == null || v2 == null )
		{
			return false;
		}

		String v1t = v1.trim();
		String v2t = v2.trim();

		if( v1t.length() == 0 && v2t.length() == 0 )
		{
			return true;
		}

		return checkEqual(trimTextValues ? v1t : v1, trimTextValues ? v2t : v2);
	}

	/**
	 * Check if the attributes of two nodes are the same.
	 */
	private boolean equalAttibutes(Node n1, Node n2)
	{
		NamedNodeMap attributes1 = n1.getAttributes();
		NamedNodeMap attributes2 = n2.getAttributes();

		if( attributes1 == null && attributes2 == null )
		{
			return true;
		}

		if( attributes1 == null || attributes2 == null )
		{
			return false;
		}

		if( attributes1.getLength() != attributes2.getLength() )
		{
			return false;
		}

		for( int i = 0; i < attributes1.getLength(); i++ )
		{
			Node attr1 = attributes1.item(i);
			Node attr2 = attributes2.getNamedItem(attr1.getNodeName());

			if( attr2 == null || !equalValues(attr1, attr2) )
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Does an equality check, but checks for nulls on either the LHS or RHS at
	 * the same time. It is true if both 'a' and 'b' are null, 'a == b', or
	 * 'a.equals(b)'
	 */
	private boolean checkEqual(Object a, Object b)
	{
		if( (a == null) ^ (b == null) )
		{
			return false;
		}
		return a == b || (a != null && a.equals(b));
	}

	/**
	 * Computes the number of size of a org.w3c.NodeList ignoring any Text
	 * nodes.
	 */
	private int sizeWithoutEmptyText(NodeList list)
	{
		int sizeMinusText = list.getLength();

		for( int i = 0; i < list.getLength(); i++ )
		{
			Node n = list.item(i);
			if( n.getNodeType() == Node.TEXT_NODE )
			{
				if( n.getNodeValue().trim().length() == 0 )
				{
					sizeMinusText--;
				}
			}
		}

		return sizeMinusText;
	}

	public void setTrimTextValues(boolean trimTextValues)
	{
		this.trimTextValues = trimTextValues;
	}

	/**
	 * Reads in an input stream to a org.w3c.Document
	 */
	public static Document getDocument(Reader xml) throws SAXException, IOException, ParserConfigurationException
	{
		InputSource inpsrc = new InputSource(xml);
		return getDocumentBuilderFactory().newDocumentBuilder().parse(inpsrc);
	}

	/**
	 * Gets a shared instance of the a document builder factory.
	 */
	private static synchronized DocumentBuilderFactory getDocumentBuilderFactory()
	{
		if( documentFactory == null )
		{
			documentFactory = DocumentBuilderFactory.newInstance();
		}
		return documentFactory;
	}
}
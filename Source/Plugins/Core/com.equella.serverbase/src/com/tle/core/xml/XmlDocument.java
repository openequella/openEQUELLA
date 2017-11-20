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

package com.tle.core.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Like PropBagEx, but heaps betterer because it supports REAL xpaths
 * 
 * @author aholland
 */
public class XmlDocument
{
	private static DocumentBuilderFactory domFactory;
	private static XPathFactory xpathFactory;

	private final Document domDoc;
	private final XPath xpathDoc;

	@SuppressWarnings("nls")
	public XmlDocument()
	{
		this("<xml/>");
	}

	public XmlDocument(Document domDoc)
	{
		this.domDoc = domDoc;
		xpathDoc = XPathFactory.newInstance().newXPath();
	}

	public XmlDocument(String xmlString)
	{
		try
		{
			domDoc = getFactory().newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		xpathDoc = getXPathFactory().newXPath();
	}

	public XmlDocument(InputStream xmlStream)
	{
		try
		{
			domDoc = getFactory().newDocumentBuilder().parse(new InputSource(new InputStreamReader(xmlStream)));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		xpathDoc = getXPathFactory().newXPath();
	}

	private static synchronized XPathFactory getXPathFactory()
	{
		if( xpathFactory == null )
		{
			xpathFactory = XPathFactory.newInstance();
		}
		return xpathFactory;
	}

	@SuppressWarnings("nls")
	private static synchronized DocumentBuilderFactory getFactory() throws ParserConfigurationException
	{
		if( domFactory == null )
		{
			domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setValidating(false);
			domFactory.setFeature("http://xml.org/sax/features/namespaces", false);
			domFactory.setFeature("http://xml.org/sax/features/validation", false);
			domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		return domFactory;
	}

	@Override
	public synchronized String toString()
	{
		return xmlToString(domDoc.getDocumentElement());
	}

	public static String xmlToString(Node node)
	{
		try
		{
			Source source = new DOMSource(node);
			StringWriter writer = new StringWriter();
			Result result = new StreamResult(writer);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);
			return writer.toString();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized Node node(String xpath)
	{
		return node(xpath, null);
	}

	public synchronized Node node(String xpath, Node nodeContext)
	{
		return singleNodeFromList(nodeList(xpath, nodeContext));
	}

	public synchronized String nodeValue(String xpath)
	{
		return nodeValue(xpath, null);
	}

	public synchronized String nodeValue(String xpath, Node nodeContext)
	{
		return singleNodeValueFromList(nodeList(xpath, nodeContext));
	}

	public synchronized List<String> nodeValues(String xpath, Node nodeContext)
	{
		return nodeValuesFromList(nodeList(xpath, nodeContext));
	}

	public synchronized NodeListIterable nodeList(String xpath)
	{
		return nodeList(xpath, null);
	}

	public synchronized NodeListIterable nodeList(String xpath, Node nodeContext)
	{
		try
		{
			if( nodeContext == null )
			{
				return new NodeListIterable((NodeList) xpathDoc.evaluate(xpath, domDoc, XPathConstants.NODESET));
			}
			return new NodeListIterable((NodeList) xpathDoc.evaluate(xpath, nodeContext, XPathConstants.NODESET));
		}
		catch( XPathExpressionException e )
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void setNodeValue(String xpath, String value)
	{

		setNodeValue(xpath, null, value);
	}

	public synchronized void setNodeValue(String xpath, Node nodeContext, String value)
	{
		Node node = singleNodeFromList(nodeList(xpath, nodeContext));
		if( node != null )
		{
			node.setTextContent(value);
		}
	}

	public synchronized void setNodeValue(String xpath, Node nodeContext, boolean value)
	{
		setNodeValue(xpath, nodeContext, Boolean.toString(value));
	}

	public synchronized void setNodeValue(String xpath, Node nodeContext, int value)
	{
		setNodeValue(xpath, nodeContext, Integer.toString(value));
	}

	public synchronized Node createNode(Node parent, String nodeName)
	{
		Node node = domDoc.createElement(nodeName);
		parent.appendChild(node);
		return node;
	}

	@SuppressWarnings("nls")
	public synchronized Node createNodeFromXPath(String xpath)
	{
		String[] xpathElements = xpath.split("/");
		StringBuilder path = new StringBuilder();
		Node node = domDoc.getDocumentElement();
		int elemLength = xpathElements.length;
		for( int i = 0; i < elemLength; i++ )
		{
			String element = xpathElements[i];
			if( !element.trim().equals("") )
			{
				path.append('/');
				path.append(element);
				Node nextNode = node(path.toString());
				// always create nodes at the bottom of the xpath:
				if( nextNode == null || i == elemLength - 1 )
				{
					node = createNode(node, element);
				}
				else
				{
					node = nextNode;
				}
			}
		}
		return node;
	}

	public synchronized Node createNodeFromXPath(String xpath, String nodeValue)
	{
		Node element = createNodeFromXPath(xpath);
		element.setTextContent(nodeValue);
		return element;
	}

	public synchronized Attr createAttribute(Node parent, String attrName)
	{
		Attr node = domDoc.createAttribute(attrName);
		parent.getAttributes().setNamedItem(node);
		return node;
	}

	public synchronized boolean deleteAll(String xpath)
	{
		return deleteAll(xpath, null);
	}

	/**
	 * @return false if no nodes deleted
	 */

	public synchronized boolean deleteAll(String xpath, Node nodeContext)
	{
		NodeListIterable nodes = nodeList(xpath, nodeContext);
		NodeListIterable.NodeListIterator nodeIter = nodes.iterator();
		if( !nodeIter.hasNext() )
		{
			return false;
		}
		while( nodeIter.hasNext() )
		{
			nodeIter.next();
			nodeIter.remove();
		}
		return true;
	}

	public synchronized Document getDOMDocument()
	{
		return domDoc;
	}

	private synchronized String singleNodeValueFromList(NodeListIterable nodeList)
	{
		Node node = singleNodeFromList(nodeList);
		if( node == null )
		{
			return null;
		}
		return getTextContent(node);
	}

	private synchronized List<String> nodeValuesFromList(NodeListIterable nodeList)
	{
		final List<String> nodeValues = new ArrayList<String>();
		for( Node node : nodeList )
		{
			nodeValues.add(getTextContent(node));
		}
		return nodeValues;
	}

	private synchronized Node singleNodeFromList(NodeListIterable nodeList)
	{
		if( nodeList.size() > 0 )
		{
			return nodeList.get(0);
		}
		return null;
	}

	public static String getTextContent(Node node)
	{
		final StringBuilder content = new StringBuilder();
		if( node.getNodeValue() != null )
		{
			content.append(node.getNodeValue());
		}

		final NodeList nodes = node.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			final Node child = nodes.item(i);
			if( child.getNodeValue() != null )
			{
				content.append(child.getNodeValue());
			}
			// recurse??
			// if( child.getChildNodes().getLength() > 0 )
			// {
			// content.append(getTextContent(child));
			// }
		}
		return content.toString().trim();
	}

	public static void renameNode(Node node, String newName)
	{
		node.getOwnerDocument().renameNode(node, null, newName);
	}

	public static class NodeListIterable implements Iterable<Node>
	{
		private final NodeList nodeList;

		public NodeListIterable(NodeList nodeList)
		{
			this.nodeList = nodeList;
		}

		// @Override
		@Override
		public NodeListIterator iterator()
		{
			return new NodeListIterator();
		}

		public Node get(int index)
		{
			return nodeList.item(index);
		}

		public int size()
		{
			return nodeList.getLength();
		}

		public class NodeListIterator implements Iterator<Node>
		{
			int index = -1;
			int length = nodeList.getLength();

			@Override
			public boolean hasNext()
			{
				return index < length - 1;
			}

			@Override
			public Node next()
			{
				return nodeList.item(++index);
			}

			@Override
			public void remove()
			{
				Node node = nodeList.item(index);
				node.getParentNode().removeChild(node);
			}
		}
	}
}
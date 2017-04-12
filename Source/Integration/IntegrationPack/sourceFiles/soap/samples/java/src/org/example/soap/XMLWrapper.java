package org.example.soap;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
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
 * 
 */
public class XMLWrapper
{
	private final Document domDoc;
	private final XPath xpathDoc;

	public XMLWrapper(String xmlString)
	{
		try
		{
			domDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(xmlString)));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		xpathDoc = XPathFactory.newInstance().newXPath();
	}

	@Override
	public String toString()
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

	public Node node(String xpath)
	{
		return node(xpath, null);
	}

	public Node node(String xpath, Node nodeContext)
	{
		return singleNodeFromList(nodeList(xpath, nodeContext));
	}

	public String nodeValue(String xpath)
	{
		return nodeValue(xpath, null);
	}

	public String nodeValue(String xpath, Node nodeContext)
	{
		return singleNodeValueFromList(nodeList(xpath, nodeContext));
	}

	public NodeList nodeList(String xpath)
	{
		return nodeList(xpath, null);
	}

	public NodeList nodeList(String xpath, Node nodeContext)
	{
		try
		{
			if( nodeContext == null )
			{
				return (NodeList) xpathDoc.evaluate(xpath, domDoc, XPathConstants.NODESET);
			}
			else
			{
				return (NodeList) xpathDoc.evaluate(xpath, nodeContext, XPathConstants.NODESET);
			}
		}
		catch( XPathExpressionException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void setNodeValue(String xpath, String value)
	{
		setNodeValue(xpath, null, value);
	}

	public void setNodeValue(String xpath, Node nodeContext, String value)
	{
		Node node = singleNodeFromList(nodeList(xpath, nodeContext));
		if( node != null )
		{
			node.setTextContent(value);
		}
	}

	public Node createNode(Node parent, String nodeName)
	{
		Node node = domDoc.createElement(nodeName);
		parent.appendChild(node);
		return node;
	}

	public Node createNodeFromXPath(String xpath)
	{
		Node node = node(xpath);
		if( node == null )
		{
			String[] xpathElements = xpath.split("/");
			String path = "";
			node = domDoc.getDocumentElement();
			for( String element : xpathElements )
			{
				if( !element.trim().equals("") )
				{
					path = path + '/' + element;
					Node nextNode = node(path);
					if( nextNode == null )
					{
						node = createNode(node, element);
					}
					else
					{
						node = nextNode;
					}
				}
			}
		}
		return node;
	}

	public Node createNodeFromXPath(String xpath, String nodeValue)
	{
		Node element = createNodeFromXPath(xpath);
		element.setTextContent(nodeValue);
		return element;
	}

	public Attr createAttribute(Node parent, String attrName)
	{
		Attr node = domDoc.createAttribute(attrName);
		parent.getAttributes().setNamedItem(node);
		return node;
	}

	private String singleNodeValueFromList(NodeList nodeList)
	{
		Node node = this.singleNodeFromList(nodeList);
		if( node == null )
		{
			return "!! node not found !!";
		}
		return node.getTextContent();
	}

	private Node singleNodeFromList(NodeList nodeList)
	{
		if( nodeList.getLength() > 0 )
		{
			return nodeList.item(0);
		}
		return null;
	}
}

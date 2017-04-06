/*
 * Created on Dec 20, 2004
 */
package com.tle.blackboard.common.propbag;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Nicholas Read
 */
final class DOMHelper
{
	private DOMHelper()
	{
		throw new IllegalAccessError("Do not invoke"); //$NON-NLS-1$
	}

	/**
	 * Removes namespace from a given path element.
	 */
	static String stripNamespace(String name)
	{
		int index = name.indexOf(':');
		if( index >= 0 )
		{
			name = name.substring(index + 1);
		}
		return name;
	}

	/**
	 * Removes namespace from a given path element.
	 */
	static String stripAttribute(String name)
	{
		if( name.startsWith("@") ) //$NON-NLS-1$
		{
			return name.substring(1);
		}
		else
		{
			return name;
		}
	}

	/**
	 * Retrieves the text value from a node's child text node.
	 */
	static String getValueForNode(Node node, String defaultValue)
	{
		String value = null;
		if( node != null )
		{
			switch( node.getNodeType() )
			{
				case Node.ELEMENT_NODE: {
					Node textNode = node.getFirstChild();
					if( textNode != null )
					{
						value = textNode.getNodeValue();
					}
					break;
				}

				case Node.ATTRIBUTE_NODE: {
					value = node.getNodeValue();
					break;
				}

				default:
					break;
			}
		}

		if( value == null )
		{
			return defaultValue;
		}
		else
		{
			return value;
		}
	}

	/**
	 * Retrieves the text value from a node's child text node.
	 */
	static void setValueForNode(Node node, String value)
	{
		if( node != null )
		{
			switch( node.getNodeType() )
			{
				case Node.ELEMENT_NODE: {
					Node child = node.getFirstChild();
					if( child == null )
					{
						Document doc = node.getOwnerDocument();
						node.appendChild(doc.createTextNode(value));
					}
					else
					{
						child.setNodeValue(value);
					}
					break;
				}

				case Node.ATTRIBUTE_NODE: {

					Attr attribute = (Attr) node;
					attribute.getOwnerElement().setAttribute(attribute.getName(), value);
					break;
				}

				default:
					break;
			}
		}
	}

	static Node findNext(Node child, String nodeName)
	{
		for( ; child != null; child = child.getNextSibling() )
		{
			if( child.getNodeType() == Node.ELEMENT_NODE )
			{
				if( nodeName == null || nodeName.equals("*") //$NON-NLS-1$
					|| nodeName.equals(DOMHelper.stripNamespace(child.getNodeName())) )
				{
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * @return a vector containing all the delimited String sections
	 */
	static List<String> splitPath(String path)
	{
		List<String> parts = new ArrayList<String>();

		String[] split = path.split("/"); //$NON-NLS-1$
		for( int i = 0; i < split.length; i++ )
		{
			if( split[i].length() > 0 )
			{
				boolean isLastPart = i == split.length - 1;
				if( !isLastPart && split[i].indexOf('@') >= 0 )
				{
					throw new IllegalArgumentException("Attribute must be last component of path"); //$NON-NLS-1$
				}
				parts.add(split[i]);
			}
		}

		return parts;
	}

	static boolean hasChildElement(Node node)
	{
		for( Node child = node.getFirstChild(); child != null; child = child.getNextSibling() )
		{
			if( child.getNodeType() == Node.ELEMENT_NODE )
			{
				return true;
			}
		}
		return false;
	}

	static boolean removeNode(Node node)
	{
		if( node != null )
		{
			switch( node.getNodeType() )
			{
				case Node.ELEMENT_NODE: {
					Node parent = node.getParentNode();
					if( parent != null )
					{
						parent.removeChild(node);
						return true;
					}
					break;
				}

				case Node.ATTRIBUTE_NODE: {
					Attr attr = (Attr) node;
					attr.getOwnerElement().removeAttribute(attr.getName());
					return true;
				}
			}
		}
		return false;
	}
}
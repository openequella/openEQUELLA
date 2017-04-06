/*
 * Created on Jun 17, 2005
 */
package com.dytech.common.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class XMLPath
{
	protected static final String SLASH = "/";
	private String node;
	private String attributeName;

	public XMLPath(String xpath)
	{
		// Only add slash to non-blank paths due to recursion
		// in XMLDataConverter
		if( xpath.length() > 0 && !xpath.startsWith(SLASH) )
		{
			xpath = SLASH + xpath;
		}

		node = xpath;

		int at = xpath.indexOf('@');
		if( at > 0 )
		{
			node = xpath.substring(0, at - 1);
			attributeName = xpath.substring(at + 1);
		}
	}

	public Object getValue(HierarchicalStreamReader reader)
	{
		Object value;
		if( attributeName != null )
		{
			value = reader.getAttribute(attributeName);
		}
		else
		{
			value = reader.getValue();
		}
		return value;
	}

	public void setValue(HierarchicalStreamWriter writer, String string)
	{
		if( attributeName != null )
		{
			writer.addAttribute(attributeName, string);
		}
		else
		{
			writer.setValue(string);
		}
	}

	public String getLastNode()
	{
		String endNode = "";
		String[] array = node.split(SLASH);

		if( array.length > 0 )
		{
			endNode = array[array.length - 1];
		}

		return endNode;
	}

	public String getNode()
	{
		return node;
	}

	public boolean hasAttribute()
	{
		return attributeName != null;
	}
}

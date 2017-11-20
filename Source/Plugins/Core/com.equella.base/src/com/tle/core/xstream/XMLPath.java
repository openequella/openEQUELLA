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

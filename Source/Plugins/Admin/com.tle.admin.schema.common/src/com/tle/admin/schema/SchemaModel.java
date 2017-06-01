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

package com.tle.admin.schema;

import com.dytech.devlib.PropBagEx;
import com.tle.admin.gui.common.GenericTreeModel;

/**
 * @author Nicholas Read
 */
public class SchemaModel extends GenericTreeModel<SchemaNode>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new SchemaModel.
	 */
	public SchemaModel()
	{
		super(new SchemaNode("xml")); //$NON-NLS-1$
	}

	/**
	 * Constructs a new SchemaModel.
	 */
	public SchemaModel(SchemaNode root)
	{
		super(root);
	}

	public void loadSchema(PropBagEx xml)
	{
		if( xml == null )
		{
			return;
		}

		String rootName = xml.getNodeName();
		SchemaNode root = new SchemaNode(rootName);
		traverseXML(xml, root);
		setRoot(root);
	}

	private void traverseXML(PropBagEx xml, SchemaNode parent)
	{
		for( PropBagEx subXml : xml.iterator() )
		{
			String name = subXml.getNodeName();
			if( !parent.hasChild(name) )
			{
				SchemaNode child = new SchemaNode(name);

				child.setAttribute(subXml.isNodeTrue("@attribute")); //$NON-NLS-1$
				child.setSearchable(subXml.isNodeTrue("@search")); //$NON-NLS-1$
				child.setField(subXml.isNodeTrue("@field")); //$NON-NLS-1$

				if( subXml.getNode("@type").equals("html") ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					child.setType("html"); //$NON-NLS-1$
				}

				traverseXML(subXml, child);
				parent.add(child);
			}
		}
	}

	private SchemaNode getChildForName(SchemaNode node, String name)
	{
		final int childCount = node.getChildCount();
		for( int j = 0; j < childCount; j++ )
		{
			SchemaNode child = (SchemaNode) node.getChildAt(j);
			String cName = child.getName();
			if( cName.equals(name) )
			{
				return child;
			}
		}
		return null;
	}

	public PropBagEx getXml()
	{
		return getRoot().getXml();
	}

	public SchemaNode getNode(String xpath)
	{
		SchemaNode node = getRoot();
		String[] elements = xpath.split("/"); //$NON-NLS-1$

		for( int i = 0; i < elements.length; i++ )
		{
			if( elements[i] != null && elements[i].length() > 0 )
			{
				if( elements[i].startsWith("@") ) //$NON-NLS-1$
				{
					elements[i] = elements[i].substring(1);
				}

				SchemaNode child = getChildForName(node, elements[i]);
				if( child == null )
				{
					return null;
				}
				else
				{
					node = child;
				}
			}
		}
		return node;
	}
}

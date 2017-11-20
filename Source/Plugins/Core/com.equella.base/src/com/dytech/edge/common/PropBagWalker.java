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

package com.dytech.edge.common;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dytech.devlib.PropBagEx;

/**
 * Visit every node of a PropBagEx (including attributes if desired) and invoke
 * a callback on the node.
 * 
 * @author aholland
 */
public class PropBagWalker
{
	private final PropBagEx propBag;

	public PropBagWalker(final PropBagEx propBag)
	{
		this.propBag = propBag;
	}

	/**
	 * Walk the tree using the passed in callback
	 * 
	 * @param visitAttributes
	 * @param callback
	 */
	public void walk(boolean visitAttributes, NodeVisitorCallback callback)
	{
		final NodeList children = propBag.getRootElement().getChildNodes();
		for( int i = 0; i < children.getLength(); ++i )
		{
			doNode(children.item(i), visitAttributes, callback);
		}
	}

	private void doNode(final Node node, final boolean visitAttributes, final NodeVisitorCallback callback)
	{
		callback.visitNode(node);

		if( visitAttributes )
		{
			final NamedNodeMap attributes = node.getAttributes();
			if( attributes != null )
			{
				for( int i = 0; i < attributes.getLength(); i++ )
				{
					doNode(attributes.item(i), visitAttributes, callback);
				}
			}
		}

		// recurse children
		final NodeList children = node.getChildNodes();
		for( int i = 0; i < children.getLength(); ++i )
		{
			doNode(children.item(i), visitAttributes, callback);
		}
	}

	protected static String getValueForNode(final Node node)
	{
		String value = null;
		if( node != null )
		{
			switch( node.getNodeType() )
			{
				case Node.ELEMENT_NODE:
					final Node textNode = node.getFirstChild();
					if( textNode != null )
					{
						value = textNode.getNodeValue();
					}
					break;

				case Node.ATTRIBUTE_NODE:
					value = node.getNodeValue();
					break;

				default:
					break;
			}
		}
		return value;
	}

	public interface NodeVisitorCallback
	{
		void visitNode(Node node);
	}

	public abstract static class NodeValueVisitorCallback implements NodeVisitorCallback
	{
		@Override
		public void visitNode(Node node)
		{
			visitNodeValue(getValueForNode(node));
		}

		public abstract void visitNodeValue(String value);
	}
}

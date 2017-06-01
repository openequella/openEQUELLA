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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.dytech.devlib.PropBagEx;
import com.tle.common.scripting.types.XmlScriptType;

/*
 * A wrapper class for PropBagEx use in Javascript.
 */
public class PropBagWrapper implements XmlScriptType
{
	private static final long serialVersionUID = 1L;
	private PropBagEx bag;
	private Stack<PathOverride> overrides;

	public PropBagWrapper(PropBagEx bag)
	{
		this.bag = bag;
	}

	public PropBagWrapper()
	{
		super();
	}

	public void clearOverrides()
	{
		if( overrides != null )
		{
			overrides.clear();
		}
	}

	public void setPropBag(PropBagEx propbag)
	{
		this.bag = propbag;
	}

	private String ensureNoSlash(String val)
	{
		if( val.length() > 0 && val.charAt(0) == '/' )
		{
			return val.substring(1);
		}
		return val;
	}

	private String ensureSlash(String val)
	{
		int len = val.length();
		if( len > 0 && val.charAt(len - 1) != '/' )
		{
			val = val + '/';
		}
		return val;
	}

	private PathOverride getOverride(String node, int ind, PropBagEx prevBag)
	{
		String n = (node == null ? "/" : node); //$NON-NLS-1$
		if( overrides != null && ind < overrides.size() )
		{
			PathOverride path = overrides.get(ind);
			n = ensureNoSlash(n);
			if( n.startsWith(path.path) )
			{
				n = n.substring(path.path.length());
				return getOverride(n, ind + 1, path.override);
			}
		}
		return new PathOverride(n, prevBag);
	}

	/**
	 * Get a node using an xpath like syntax. If there is more than one node
	 * with that xpath, it will return the value in the first one.
	 * 
	 * @param node The xpath to get the value from
	 * @return The value from the xml document
	 */
	@Override
	public String get(String node)
	{
		PathOverride override = getOverride(node, 0, bag);
		return override.getNode();
	}

	/**
	 * Set a node's value
	 * 
	 * @param node The xpath to the node
	 * @param value The value to set
	 */
	@Override
	public void set(String node, String value)
	{
		PathOverride override = getOverride(node, 0, bag);
		override.setNode(value);
	}

	public void setOrAdd(String node, String value)
	{
		PathOverride override = getOverride(node, 0, bag);
		if( override.exists() )
		{
			override.setNode(value);
		}
		else
		{
			override.createNode(value);
		}
	}

	/**
	 * Find out if any value of the nodes with a given xpath match a certain
	 * value.
	 * 
	 * @param node The xpath to the node(s)
	 * @param value The value to check for
	 * @return true if the value is found
	 */
	@Override
	public boolean contains(String node, String value)
	{
		PathOverride override = getOverride(node, 0, bag);
		PropBagEx actbag = override.override;
		if( actbag == null )
		{
			return false;
		}
		node = override.path;
		if( node.indexOf('@') >= 0 )
		{
			return actbag.getNode(node).equals(value);
		}

		for( String text : actbag.iterateAllValues(node) )
		{
			if( text.equals(value) )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all values of a given xpath.
	 * 
	 * @param node The xpath to the node(s)
	 * @return All values for that node
	 */
	@Override
	public String[] getAll(String node)
	{
		PathOverride override = getOverride(node, 0, bag);
		return override.getAllNodes();
	}

	@Override
	public List<String> list(String xpath)
	{
		return Arrays.asList(getAll(xpath));
	}

	@Override
	public XmlScriptType[] getAllSubtrees(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.getAllSubtrees();
	}

	/**
	 * Add a new node with and set its value
	 * 
	 * @param node The xpath to the new node
	 * @param value The value to set
	 */
	@Override
	public void add(String node, String value)
	{
		// PropBag doesn't check for a null value and causes it
		// to blow up when toStringed.
		if( value == null )
		{
			throw new RuntimeException("Node value cannot be null"); //$NON-NLS-1$
		}
		PathOverride override = getOverride(node, 0, bag);
		override.createNode(value);
	}

	/**
	 * @param src
	 * @param dest
	 */
	@Override
	public void copy(String src, String dest)
	{
		PathOverride oversrc = getOverride(src, 0, bag);
		PathOverride overdest = getOverride(dest, 0, bag);
		PropBagEx srcbag = oversrc.override;
		src = oversrc.path;
		PropBagEx destbag = overdest.override;
		dest = overdest.path;
		if( srcbag == null || destbag == null )
		{
			return;
		}
		int nCount = srcbag.nodeCount(src);
		for( int i = 0; i < nCount; i++ )
		{
			destbag.createNode(dest, srcbag.getNode(src));
		}
	}

	@Override
	public void deleteNode(String node)
	{
		PathOverride override = getOverride(node, 0, bag);
		override.deleteNode();
	}

	@Override
	public void deleteAll(String node)
	{
		PathOverride override = getOverride(node, 0, bag);
		override.deleteAllNodes();
	}

	@Override
	public void clear()
	{
		bag.clear();
	}

	@Override
	public boolean exists(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.exists();
	}

	@Override
	public int count(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.count();
	}

	@Override
	public String asString()
	{
		PathOverride override = getOverride("/", 0, bag); //$NON-NLS-1$
		if( override.override == null )
		{
			return ""; //$NON-NLS-1$
		}
		return override.override.toString();
	}

	@Override
	public XmlScriptType findForId(String id)
	{
		PathOverride override = getOverride("/", 0, bag); //$NON-NLS-1$
		return override.findForId(id);
	}

	@Override
	public PropBagWrapper getSubtree(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.getSubtree();
	}

	@Override
	public XmlScriptType getOrCreateSubtree(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.getOrCreateSubtree();
	}

	@Override
	public XmlScriptType createSubtree(String xpath)
	{
		PathOverride override = getOverride(xpath, 0, bag);
		return override.createSubtree();
	}

	@Override
	public void deleteSubtree(XmlScriptType subtree)
	{
		if( subtree != null )
		{
			PathOverride override = getOverride("/", 0, bag); //$NON-NLS-1$
			override.deleteSubtree((PropBagWrapper) subtree);
		}
	}

	@Override
	public void append(String xpath, XmlScriptType documentToAppend)
	{
		if( documentToAppend != null )
		{
			PathOverride override = getOverride(xpath, 0, bag);
			override.append(xpath, (PropBagWrapper) documentToAppend);
		}
	}

	@Override
	public void appendChildren(String xpath, XmlScriptType documentToAppend)
	{
		if( documentToAppend != null )
		{
			PathOverride override = getOverride(xpath, 0, bag);
			override.appendChildren(xpath, (PropBagWrapper) documentToAppend);
		}
	}

	private static class PathOverride
	{
		String path;
		PropBagEx override;

		public PathOverride(String node, PropBagEx prevBag)
		{
			path = node;
			override = prevBag;
		}

		public String getNode()
		{
			if( override == null )
			{
				return ""; //$NON-NLS-1$
			}
			return override.getNode(path);
		}

		public String[] getAllNodes()
		{
			Collection<String> results = new ArrayList<String>();
			if( override != null )
			{
				for( String value : override.iterateAllValues(path) )
				{
					results.add(value);
				}
			}
			return results.toArray(new String[results.size()]);
		}

		public void setNode(String value)
		{
			if( override == null )
			{
				return;
			}
			override.setNode(path, value);
		}

		public void createNode(String value)
		{
			if( override == null )
			{
				return;
			}
			override.createNode(path, value);
		}

		public boolean exists()
		{
			if( override == null )
			{
				return false;
			}
			return override.nodeExists(path);
		}

		public int count()
		{
			if( override == null )
			{
				return 0;
			}
			return override.nodeCount(path);
		}

		public void deleteNode()
		{
			if( override == null )
			{
				return;
			}
			override.deleteNode(path);
		}

		public void deleteAllNodes()
		{
			if( override == null )
			{
				return;
			}
			override.deleteAll(path);
		}

		public PropBagWrapper findForId(String id)
		{
			if( override == null )
			{
				return null;
			}
			return findForId(override, id);
		}

		private PropBagWrapper findForId(PropBagEx xml, String id)
		{
			for( PropBagEx sxml : xml.iterator() )
			{
				if( sxml.getNode("@id").equals(id) ) //$NON-NLS-1$
				{
					return new PropBagWrapper(sxml);
				}

				PropBagWrapper pbw = findForId(sxml, id);
				if( pbw != null )
				{
					return pbw;
				}
			}
			return null;
		}

		public PropBagWrapper getSubtree()
		{
			if( override == null )
			{
				return null;
			}
			PropBagEx subTree = override.getSubtree(path);
			if( subTree == null )
			{
				return null;
			}
			return new PropBagWrapper(subTree);
		}

		public PropBagWrapper getOrCreateSubtree()
		{
			if( override == null )
			{
				return null;
			}
			PropBagEx subTree = override.aquireSubtree(path);
			if( subTree == null )
			{
				return null;
			}
			return new PropBagWrapper(subTree);
		}

		public PropBagWrapper[] getAllSubtrees()
		{
			List<PropBagWrapper> subTrees = new ArrayList<PropBagWrapper>();
			if( override != null )
			{
				for( PropBagEx subTree : override.iterateAll(path) )
				{
					subTrees.add(new PropBagWrapper(subTree));
				}
			}
			return subTrees.toArray(new PropBagWrapper[subTrees.size()]);
		}

		public PropBagWrapper createSubtree()
		{
			if( override == null )
			{
				return null;
			}
			return new PropBagWrapper(override.newSubtree(path));
		}

		public void deleteSubtree(PropBagWrapper subtree)
		{
			if( override != null )
			{
				override.deleteSubtree(subtree.bag);
			}
		}

		public void append(String path, PropBagWrapper docToAppend)
		{
			if( override == null )
			{
				return;
			}
			override.append(path, docToAppend.bag);
		}

		public void appendChildren(String path, PropBagWrapper docToAppend)
		{
			if( override == null )
			{
				return;
			}
			override.appendChildren(path, docToAppend.bag);
		}
	}

	public void pushOverride(String path, int index)
	{
		if( overrides == null )
		{
			overrides = new Stack<PathOverride>();
		}

		PropBagEx base = bag;
		if( !overrides.isEmpty() )
		{
			base = overrides.peek().override;
		}

		PropBagEx subtree = null;
		if( base != null )
		{
			subtree = base.getSubtree(path + '[' + index + ']');
		}

		overrides.add(new PathOverride(ensureNoSlash(ensureSlash(path)), subtree));
	}

	public void popOverride()
	{
		overrides.pop();
	}

	@Override
	public String toString()
	{
		return asString();
	}
}

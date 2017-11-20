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

package com.tle.admin.security.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.text.NumberStringComparator;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public abstract class AbstractLazyNode implements SecurityTreeNode
{
	private static final Log LOGGER = LogFactory.getLog(AbstractLazyNode.class);

	private final String displayName;
	private final Node privNode;

	private SecurityTreeNode parent;
	private List<SecurityTreeNode> children;

	public AbstractLazyNode(String displayName, Node privNode)
	{
		this.displayName = displayName;
		this.privNode = privNode;
	}

	protected abstract List<SecurityTreeNode> getChildren();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.model.SecurityTreeNode#getPrivilegeNode()
	 */
	@Override
	public Node getPrivilegeNode()
	{
		return privNode;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.SecurityTreeNode#setParent(com.tle.admin.
	 * security.tree.SecurityTreeNode)
	 */
	@Override
	public void setParent(SecurityTreeNode parent)
	{
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeNode#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	@Override
	public synchronized TreeNode getChildAt(int childIndex)
	{
		ensureChildren();
		return children.get(childIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	@Override
	public synchronized int getChildCount()
	{
		ensureChildren();
		return children.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	@Override
	public synchronized int getIndex(TreeNode node)
	{
		ensureChildren();
		return children.indexOf(node);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	@Override
	public synchronized boolean isLeaf()
	{
		return children == null || children.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#children()
	 */
	@Override
	public Enumeration<SecurityTreeNode> children()
	{
		ensureChildren();
		return new Enumeration<SecurityTreeNode>()
		{
			private Iterator<SecurityTreeNode> iter = children.iterator();

			/*
			 * (non-Javadoc)
			 * @see java.util.Enumeration#hasMoreElements()
			 */
			@Override
			public boolean hasMoreElements()
			{
				return iter.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Enumeration#nextElement()
			 */
			@Override
			public SecurityTreeNode nextElement()
			{
				return iter.next();
			}
		};
	}

	private synchronized void ensureChildren()
	{
		if( children == null )
		{
			try
			{
				children = new ArrayList<SecurityTreeNode>(getChildren());
			}
			catch( Exception e )
			{
				// We might not have permissions - this shouldn't happen
				// though
				LOGGER.error("An error occurred", e);
				children = new ArrayList<SecurityTreeNode>();
			}

			// Make sure this is the parent of all the children
			for( SecurityTreeNode child : children )
			{
				child.setParent(this);
			}

			// Sort them nicely...
			Collections.sort(children, new NumberStringComparator<SecurityTreeNode>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(SecurityTreeNode t)
				{
					return t.getDisplayName();
				}
			});

		}
	}
}

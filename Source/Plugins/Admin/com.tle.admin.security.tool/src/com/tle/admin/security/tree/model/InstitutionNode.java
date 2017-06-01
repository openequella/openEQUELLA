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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class InstitutionNode implements SecurityTreeNode
{
	private final List<SecurityTreeNode> children;

	public InstitutionNode()
	{
		this.children = new ArrayList<SecurityTreeNode>();
	}

	public void addChild(SecurityTreeNode node)
	{
		children.add(node);
		node.setParent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getTargetObject()
	 */
	@Override
	public Object getTargetObject()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.model.SecurityTreeNode#getPrivilegeNode()
	 */
	@Override
	public Node getPrivilegeNode()
	{
		return Node.INSTITUTION;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.SecurityTreeNode#setParent(com.tle.admin.
	 * security.tree.SecurityTreeNode)
	 */
	@Override
	public void setParent(SecurityTreeNode node)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeNode#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.admin.security.tree.model.institutionnode.name"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return children.get(childIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount()
	{
		return children.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	@Override
	public int getIndex(TreeNode node)
	{
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
	public boolean isLeaf()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#children()
	 */
	@Override
	public Enumeration<SecurityTreeNode> children()
	{
		return new Enumeration<SecurityTreeNode>()
		{
			private Iterator<SecurityTreeNode> iter = children.iterator();

			@Override
			public boolean hasMoreElements()
			{
				return iter.hasNext();
			}

			@Override
			public SecurityTreeNode nextElement()
			{
				return iter.next();
			}
		};
	}
}

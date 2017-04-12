package com.tle.admin.security.tree.model;

import javax.swing.tree.TreeNode;

import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public interface SecurityTreeNode extends TreeNode
{
	/**
	 * Name as should appear in the tree.
	 */
	String getDisplayName();

	/**
	 * The privilege node for this node type.
	 */
	Node getPrivilegeNode();

	/**
	 * Return <code>null</code> if this is a virtual group with no real target
	 * object.
	 */
	Object getTargetObject();

	/**
	 * The parent
	 */
	void setParent(SecurityTreeNode node);
}

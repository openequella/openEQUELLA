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

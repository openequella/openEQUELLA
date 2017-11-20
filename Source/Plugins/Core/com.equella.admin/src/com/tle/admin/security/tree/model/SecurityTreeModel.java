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

import static com.tle.common.security.PrivilegeTree.Node.ALL_COLLECTIONS;
import static com.tle.common.security.PrivilegeTree.Node.ALL_COURSE_INFO;
import static com.tle.common.security.PrivilegeTree.Node.ALL_FEDERATED_SEARCHES;
import static com.tle.common.security.PrivilegeTree.Node.ALL_POWER_SEARCHES;
import static com.tle.common.security.PrivilegeTree.Node.ALL_SCHEMAS;
import static com.tle.common.security.PrivilegeTree.Node.COLLECTION;
import static com.tle.common.security.PrivilegeTree.Node.COURSE_INFO;
import static com.tle.common.security.PrivilegeTree.Node.FEDERATED_SEARCH;
import static com.tle.common.security.PrivilegeTree.Node.POWER_SEARCH;
import static com.tle.common.security.PrivilegeTree.Node.SCHEMA;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemotePrivilegeTreeService;
import com.tle.core.plugins.PluginService;
import com.tle.core.remoting.RemoteCourseInfoService;
import com.tle.core.remoting.RemoteFederatedSearchService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.core.remoting.RemoteSchemaService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class SecurityTreeModel implements TreeModel
{
	private final DefaultTreeModel model;

	public SecurityTreeModel(final ClientService services, final PluginService pluginService)
	{
		InstitutionNode root = new InstitutionNode();

		root.addChild(new EntityGroupNode(CurrentLocale
			.get("com.tle.admin.security.tree.model.securitytreemodel.collections"), ALL_COLLECTIONS, COLLECTION,
			services.getService(RemoteItemDefinitionService.class))
		{
			@Override
			protected SecurityTreeNode createNode(BaseEntityLabel label, Node nodeType)
			{
				return new ItemDefinitionNode(label, services.getService(RemoteItemDefinitionService.class));
			}
		});

		root.addChild(new EntityGroupNode(CurrentLocale
			.get("com.tle.admin.security.tree.model.securitytreemodel.powersearches"), ALL_POWER_SEARCHES,
			POWER_SEARCH, services.getService(RemotePowerSearchService.class)));

		root.addChild(new EntityGroupNode(CurrentLocale
			.get("com.tle.admin.security.tree.model.securitytreemodel.schemas"), ALL_SCHEMAS, SCHEMA, services
			.getService(RemoteSchemaService.class)));

		root.addChild(new EntityGroupNode(CurrentLocale
			.get("com.tle.admin.security.tree.model.securitytreemodel.fedsearches"), ALL_FEDERATED_SEARCHES,
			FEDERATED_SEARCH, services.getService(RemoteFederatedSearchService.class)));

		root.addChild(new EntityGroupNode(CurrentLocale
			.get("com.tle.admin.security.tree.model.securitytreemodel.courses"), ALL_COURSE_INFO, COURSE_INFO, services
			.getService(RemoteCourseInfoService.class)));

		// The following is the way of the future! Everything above should be
		// deleted in favour of the following approach.
		final RemotePrivilegeTreeService remotePrivilegeTreeService = services
			.getService(RemotePrivilegeTreeService.class);
		final List<SecurityTreeNode> nodes = DynamicPrivilegeTreeNode.getSecurityTargetsAsTreeNodes(pluginService,
			remotePrivilegeTreeService, null);
		for( SecurityTreeNode node : nodes )
		{
			root.addChild(node);
		}

		// Hard-coded bit :(

		root.addChild(new ItemSearchNode());

		model = new DefaultTreeModel(root, true);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot()
	{
		return model.getRoot();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index)
	{
		return model.getChild(parent, index);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent)
	{
		return model.getChildCount(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node)
	{
		return model.isLeaf(node);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		model.valueForPathChanged(path, newValue);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		return model.getIndexOfChild(parent, child);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		model.addTreeModelListener(l);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		model.removeTreeModelListener(l);
	}

	public TreePath getPathToRoot(TreeNode node)
	{
		return new TreePath(model.getPathToRoot(node));
	}

	public void nodeStructureChanged(SecurityTreeNode node)
	{
		model.nodeStructureChanged(node);
	}
}

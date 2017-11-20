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

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Nicholas Read
 * @dytech.jira see Jira Defect TLE-594 :
 *              http://apps.dytech.com.au/jira/browse/TLE-594
 * @dytech.jira see Jira Defect TLE-1711 :
 *              http://apps.dytech.com.au/jira/browse/TLE-1711
 */
public class SchemaTree extends JTree implements TreeModelListener
{
	private static final long serialVersionUID = 1L;
	private SchemaModel model;

	/**
	 * Constructs a new SchemaTree.
	 */
	public SchemaTree()
	{
		this(new SchemaModel());
	}

	/**
	 * Constructs a new SchemaTree.
	 */
	public SchemaTree(SchemaModel model)
	{
		this(model, false);
	}

	/**
	 * Constructs a new SchemaTree.
	 */
	public SchemaTree(SchemaModel model, boolean greyNonIndexed)
	{
		super();

		this.model = model;
		model.addTreeModelListener(this);
		setModel(model.getUnderlyingTreeModel());

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(new SchemaRenderer(greyNonIndexed));
	}

	public SchemaModel getSchemaModel()
	{
		return model;
	}

	public void expandToNode(SchemaNode node)
	{
		TreePath path = new TreePath(node.getPath());
		setExpandedState(path, true);
		updateUI();
	}

	@Override
	public boolean isPathEditable(TreePath path)
	{
		SchemaNode node = (SchemaNode) path.getLastPathComponent();

		if( node.isLocked() || node.isRoot() )
		{
			return false;
		}
		else
		{
			return isEditable();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
		if( e.getChildren().length >= 1 )
		{
			expandToNode((SchemaNode) e.getChildren()[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.
	 * event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
		if( model.getRoot().getChildCount() > 0 )
		{
			expandToNode((SchemaNode) model.getRoot().getChildAt(0));
		}
	}
}

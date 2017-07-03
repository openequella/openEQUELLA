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

package com.tle.admin.workflow.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.tle.admin.Driver;
import com.tle.admin.workflow.StepDialog;
import com.tle.admin.workflow.WorkflowCellRenderer;
import com.tle.admin.workflow.editor.DecisionEditor;
import com.tle.admin.workflow.editor.NodeEditor;
import com.tle.admin.workflow.editor.ScriptEditor;
import com.tle.admin.workflow.editor.StepEditor;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;

public class WorkflowTree extends JTree
{
	private static final long serialVersionUID = 1L;
	private final WorkflowTreeModel model;
	private final RemoteUserService userService;
	private final RemoteSchemaService schemaService;

	public WorkflowTree(final WorkflowTreeModel model, final RemoteUserService userService,
		final RemoteSchemaService schemaService)
	{
		super(model);

		this.model = model;
		this.userService = userService;
		this.schemaService = schemaService;

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(new WorkflowCellRenderer());
		addMouseListener(new WorkflowTreeMouseAdapter());
		setShowsRootHandles(true);
		model.addTreeModelListener(new WorkflowTreeModelAdapter());
	}

	public WorkflowNode getSelectedNode()
	{
		final TreePath path = getSelectionPath();
		WorkflowNode node = null;
		if( path != null )
		{
			node = (WorkflowNode) path.getLastPathComponent();
		}
		return node;
	}

	public void add()
	{
		final WorkflowNode parent = getSelectedNode();
		if( parent == null || parent.canAddChildren() )
		{
			final Class<? extends WorkflowNode> nodeClass = new StepDialog().promptForSelection(this);
			if( nodeClass != null )
			{
				try
				{
					WorkflowNode newNode = nodeClass.getConstructor(LanguageBundle.class).newInstance(
						new Object[]{null});
					model.add((WorkflowTreeNode) parent, newNode);
					modify(newNode);
				}
				catch( final Exception e )
				{
					throw new RuntimeException("Error occured creating step instance", e);
				}
			}
		}
	}

	public void remove()
	{
		final WorkflowNode parent = getSelectedNode();
		if( parent != null )
		{
			final String message = CurrentLocale.get(
				"com.tle.admin.workflow.tree.workflowtree.confirm", //$NON-NLS-1$
				CurrentLocale.get(parent.getName(),
					CurrentLocale.get("com.tle.admin.workflow.tree.workflowcellrenderer.unnamed"))); //$NON-NLS-1$
			if( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, message, null, JOptionPane.YES_NO_OPTION) )
			{
				model.remove(getSelectedNode());
			}
		}
	}

	protected class WorkflowTreeModelAdapter extends TreeModelHandler
	{
		@Override
		public void treeNodesInserted(final TreeModelEvent e)
		{
			// Needs to be done like this otherwise funny things happen
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					expandPath(e.getTreePath());
				}
			});
		}
	}

	private class WorkflowTreeMouseAdapter extends MouseAdapter implements ActionListener
	{
		private final JMenuItem add;
		private final JMenuItem remove;
		private final JMenuItem edit;
		private final JPopupMenu menu;
		private boolean visible;

		protected WorkflowTreeMouseAdapter()
		{
			menu = new JPopupMenu();
			add = new JMenuItem(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
			add.addActionListener(this);
			remove = new JMenuItem(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$
			remove.addActionListener(this);
			edit = new JMenuItem(CurrentLocale.get("com.tle.admin.edit")); //$NON-NLS-1$
			edit.addActionListener(this);
			menu.add(add);
			menu.add(edit);
			menu.add(remove);
		}

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final Object source = e.getSource();
			if( source.equals(add) )
			{
				add();
			}
			else if( source.equals(remove) )
			{
				remove();
			}
			else if( source.equals(edit) )
			{
				modify();
			}
		}

		@Override
		public void mousePressed(final MouseEvent e)
		{
			if( e.getButton() == MouseEvent.BUTTON3 )
			{
				setSelectionPath(getPathForLocation(e.getX(), e.getY()));
			}
			final WorkflowNode node = getSelectedNode();
			final boolean canedit = node != null && node.getParent() != model.getRoot();
			visible = (node == null && model.getRootNode() == null) || (node != null && node.canAddChildren());

			add.setVisible(visible);
			edit.setVisible(canedit);
			remove.setVisible(canedit);
			visible |= node != null;
		}

		@Override
		public void mouseReleased(final MouseEvent e)
		{
			if( e.isPopupTrigger() && visible )
			{
				setSelectionPath(getPathForLocation(e.getX(), e.getY()));
				menu.show(WorkflowTree.this, e.getX(), e.getY());
			}
		}
	}

	public void modify()
	{
		modify(getSelectedNode());
	}

	public void modify(final WorkflowNode node)
	{
		if( node != null && node.getParent() != null )
		{
			NodeEditor wid;
			if( node.getType() == WorkflowNode.ITEM_TYPE )
			{
				wid = new StepEditor(userService, schemaService);
			}
			else if( node.getType() == WorkflowNode.DECISION_TYPE )
			{
				wid = new DecisionEditor(Driver.instance());
			}
			else if( node.getType() == WorkflowNode.SCRIPT_TYPE )
			{
				wid = new ScriptEditor(userService, schemaService);
			}
			else
			{
				wid = new NodeEditor(userService, schemaService, "com.tle.admin.workflow.editor.nodeeditor.title"); //$NON-NLS-1$
			}

			wid.load(node);

			if( wid.showEditor(this) )
			{
				wid.save(node);
				model.nodeChanged(node);
			}
		}
	}

	public void up()
	{
		final WorkflowNode node = getSelectedNode();
		model.up(node);
		setSelectionPath(new TreePath(model.getPathToRoot(node)));
	}

	public void down()
	{
		final WorkflowNode node = getSelectedNode();
		model.down(node);
		setSelectionPath(new TreePath(model.getPathToRoot(node)));
	}
}

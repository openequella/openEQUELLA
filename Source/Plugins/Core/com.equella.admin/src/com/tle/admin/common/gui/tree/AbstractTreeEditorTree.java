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

package com.tle.admin.common.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.actions.AddChildAction;
import com.tle.admin.gui.common.actions.AddSiblingAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.client.gui.popup.TreePopupListener;
import com.tle.common.Check;
import com.tle.common.LazyTreeNode;
import com.tle.common.LazyTreeNode.ChildrenState;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.AbstractPluginService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class AbstractTreeEditorTree<NodeType extends LazyTreeNode> extends JPanel
	implements
		TreeNodeChangeListener
{
	private final boolean canAddRootNodes;

	protected JTree tree;
	protected NodeType root;
	protected DefaultTreeModel model;

	protected List<TLEAction> actions;

	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(getKey(key));
	}

	protected String getKey(String key)
	{
		return KEY_PFX+key;
	}

	public AbstractTreeEditorTree(boolean canAddRootNodes)
	{
		this.canAddRootNodes = canAddRootNodes;
	}

	public final NodeType getRootNode()
	{
		return root;
	}

	/**
	 * Performed initial setup and initialisation. This method should only ever
	 * be invoked once.
	 */
	protected void setup()
	{
		actions = new ArrayList<TLEAction>();
		actions.add(moveUpAction);
		actions.add(moveDownAction);
		actions.add(addChildAction);
		actions.add(addSiblingAction);
		actions.add(removeAction);

		setupAdditionalActions(actions);

		setupGui();
	}

	protected void setupAdditionalActions(List<TLEAction> actions)
	{
		// Nothing by default
	}

	protected abstract NodeType createNode();

	public abstract boolean canEdit(NodeType node);

	protected abstract boolean preAddNewNode(NodeType parent, NodeType newNode, Map<Object, Object> params);

	protected abstract void doAddNewNode(NodeType parent, NodeType newNode, Map<Object, Object> params);

	protected abstract List<NodeType> doListNodes(final NodeType parent);

	protected abstract void doDelete(final NodeType node);

	protected abstract void doMove(final NodeType node, final NodeType parent, final int position);

	private LazyTreeNode getLoadingTreeNode()
	{
		NodeType node = createNode();
		node.setName(CurrentLocale.get("com.tle.admin.gui.common.tree.editor.loading"));
		node.setAllowsChildren(false);
		node.setChildrenState(ChildrenState.LOADED);
		return node;
	}

	private void setupGui()
	{
		root = createNode();
		root.setChildrenState(ChildrenState.UNLOADED);

		model = new DefaultTreeModel(root)
		{
			@Override
			@SuppressWarnings("unchecked")
			public int getChildCount(Object parent)
			{
				NodeType node = (NodeType) parent;
				if( node.getChildrenState() == ChildrenState.UNLOADED )
				{
					loadChildren(node);
				}
				return super.getChildCount(parent);
			}
		};

		tree = new JTree(model);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new TreePopupListener(tree, actions));
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				updateActions();
			}
		});

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
		{
			@Override
			@SuppressWarnings("unchecked")
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

				NodeType node = (NodeType) value;
				if( !canEdit(node) )
				{
					setForeground(Color.GRAY);
				}

				return this;
			}
		};
		renderer.setLeafIcon(renderer.getClosedIcon());
		tree.setCellRenderer(renderer);

		// We need to ensure this is set *after* the model has been set on the
		// JTree, otherwise the getChildCount() method is invoked before any
		// subclasses of this component have been constructed.
		model.setAsksAllowsChildren(true);

		JScrollPane scroll = new JScrollPane(tree);

		JButton upButton = new JTextlessButton(moveUpAction);
		JButton downButton = new JTextlessButton(moveDownAction);

		JPanel bottom = createButtonArea();

		int width = upButton.getPreferredSize().width;
		int height1 = upButton.getPreferredSize().height;
		int height2 = bottom.getPreferredSize().height;

		int[] rows = {TableLayout.FILL, height1, height1, TableLayout.FILL, height2,};
		int[] cols = {width, TableLayout.FILL,};
		setLayout(new TableLayout(rows, cols, 5, 5));

		add(upButton, new Rectangle(0, 1, 1, 1));
		add(downButton, new Rectangle(0, 2, 1, 1));
		add(scroll, new Rectangle(1, 0, 1, 4));
		add(bottom, new Rectangle(1, 4, 1, 1));

		updateActions();
	}

	private JPanel createButtonArea()
	{
		JButton childButton = new JButton(addChildAction);
		JButton siblingButton = new JButton(addSiblingAction);
		JButton removeButton = new JButton(removeAction);

		JPanel all = new JPanel(new FlowLayout());
		all.add(childButton);
		all.add(siblingButton);
		all.add(removeButton);

		all.setPreferredSize(all.getMinimumSize());

		return all;
	}

	public final void addTreeSelectionListener(TreeSelectionListener l)
	{
		tree.addTreeSelectionListener(l);
	}

	public final void removeTreeSelectionListener(TreeSelectionListener l)
	{
		tree.removeTreeSelectionListener(l);
	}

	protected final void updateActions()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
	}

	private final TLEAction moveUpAction = new UpAction()
	{

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			doMoveGui(getSelectedNode(), -1);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void update()
		{
			boolean b = tree.getSelectionCount() == 1;
			if( b )
			{
				NodeType node = getSelectedNode();
				b = canEdit(node);
				if( b )
				{
					node = (NodeType) node.getPreviousSibling();
					b = node != null && canEdit(node);
				}
			}
			setEnabled(b);
		}
	};

	private final TLEAction moveDownAction = new DownAction()
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			doMoveGui(getSelectedNode(), 1);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void update()
		{
			boolean b = tree.getSelectionCount() == 1;
			if( b )
			{
				NodeType node = getSelectedNode();
				b = canEdit(node);
				if( b )
				{
					node = (NodeType) node.getNextSibling();
					b = node != null && canEdit(node);
				}
			}
			setEnabled(b);
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final NodeType node = getSelectedNode();

			final int result = JOptionPane.showConfirmDialog(AbstractTreeEditorTree.this,
				CurrentLocale.get("com.tle.admin.gui.common.tree.editor.confirmremove", node.getName()),
				CurrentLocale.get("com.tle.admin.gui.common.tree.editor.confirmremovetitle"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if( result == JOptionPane.YES_OPTION )
			{
				GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
				{
					@Override
					public Object construct()
					{
						doDelete(node);
						return null;
					}

					@Override
					public void finished()
					{
						model.removeNodeFromParent(node);
						selectTopic(null);
					}

					@Override
					public void exception()
					{
						getException().printStackTrace();
						JOptionPane.showMessageDialog(getComponent(),
							"An error has occurred while removing the selected nodes\n"
								+ "and children (if any).  This operation may still successfully complete\n"
								+ "on the server.  The tree of nodes will now be reloaded in order to\n"
								+ "keep this editor synchronised with the server.", "Error",
							JOptionPane.WARNING_MESSAGE);

						root.setChildrenState(ChildrenState.UNLOADED);
						model.reload(root);
					}
				};
				worker.setComponent(AbstractTreeEditorTree.this);
				worker.start();
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void update()
		{
			setEnabled(false);

			if( tree.getSelectionCount() > 0 )
			{
				for( TreePath path : tree.getSelectionPaths() )
				{
					if( !canEdit((NodeType) path.getLastPathComponent()) )
					{
						return;
					}
				}
			}

			setEnabled(true);
		}
	};

	private final TLEAction addChildAction = new AddChildAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			NodeType rootNode = getRootNode();
			NodeType selected = getSelectedNode();
			if( rootNode.getChildCount() > 0 && selected != null )
			{
				addNewChild(selected);
			}
			else
			{
				addNewChild(rootNode);
			}
		}

		@Override
		public void update()
		{
			boolean nodeSelected = tree.getSelectionCount() == 1 && canEdit(getSelectedNode());
			boolean rootHasChildren = canAddRootNodes && getRootNode().getChildCount() == 0;
			setEnabled(nodeSelected || rootHasChildren);
		}
	};

	private final TLEAction addSiblingAction = new AddSiblingAction()
	{
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e)
		{
			addNewChild((NodeType) getSelectedNode().getParent());
		}

		@Override
		@SuppressWarnings("unchecked")
		public void update()
		{
			setEnabled(tree.getSelectionCount() == 1 && canEdit((NodeType) getSelectedNode().getParent()));
		}
	};

	private void doMoveGui(final NodeType node, final int offset)
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			private NodeType parent;
			private int realPosition;

			@Override
			@SuppressWarnings("unchecked")
			public Object construct()
			{
				parent = (NodeType) node.getParent();
				realPosition = parent.getIndex(node) + offset;

				doMove(node, parent, realPosition);
				return null;
			}

			@Override
			public void finished()
			{
				model.insertNodeInto(node, parent, realPosition);
				selectTopic(node);
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	protected String promptForName()
	{
		String name = null;
		do
		{
			name = JOptionPane.showInputDialog(this,
				CurrentLocale.get("com.tle.admin.gui.common.tree.editor.entername"));
			if( name == null )
			{
				return null;
			}
		}
		while( Check.isEmpty(name) || !isProposedNewNameValid(name) );

		return name;
	}

	protected final void addNewChild(final NodeType parent)
	{
		final String name = promptForName();
		if( name == null )
		{
			return;
		}

		final NodeType newNode = createNode();
		newNode.setName(name.trim());

		final Map<Object, Object> params = new HashMap<Object, Object>();
		if( !preAddNewNode(parent, newNode, params) )
		{
			return;
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct()
			{
				doAddNewNode(parent, newNode, params);
				return null;
			}

			@Override
			public void finished()
			{
				model.insertNodeInto(newNode, parent, parent.getChildCount());
				selectTopic(newNode);
				tree.expandPath(new TreePath(model.getPathToRoot(newNode)));
			}

			@Override
			public void exception()
			{
				JOptionPane.showMessageDialog(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.tree.editor.addingerror"));
				getException().printStackTrace();
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	protected boolean isProposedNewNameValid(String proposedName)
	{
		return true;
	}

	public final void loadChildren(final NodeType parent)
	{
		switch( parent.getChildrenState() )
		{
			case LOADED:
			case LOADING:
				// Nothing to do here
				break;

			case UNLOADED:
				// We don't want to do this through the model...
				parent.removeAllChildren();
				parent.add(getLoadingTreeNode());
				parent.setChildrenState(ChildrenState.LOADING);

				GlassSwingWorker<?> worker = new GlassSwingWorker<List<NodeType>>()
				{
					@Override
					public List<NodeType> construct() throws Exception
					{
						return doListNodes(parent);
					}

					@Override
					public void finished()
					{
						parent.removeAllChildren();
						List<NodeType> newChildren = get();
						if( newChildren == null )
						{
							// Tree was not ready, so reset node to UNLOADED
							parent.setChildrenState(ChildrenState.UNLOADED);
						}
						else
						{
							for( NodeType child : get() )
							{
								parent.add(child);
							}
							parent.setChildrenState(ChildrenState.LOADED);
						}

						model.nodeStructureChanged(parent);

						updateActions();
					}

					@Override
					public void exception()
					{
						getException().printStackTrace();
					}
				};
				worker.setComponent(this);
				worker.start();
				break;
		}
	}

	@Override
	public void nodeSaved(LazyTreeNode node)
	{
		// We invoke a nodeStructureChanged event so that all children
		// nodes are re-rendered. This is in case the edit privileges
		// have changed.
		model.nodeStructureChanged(node);
	}

	@SuppressWarnings("unchecked")
	protected NodeType getSelectedNode()
	{
		return (NodeType) tree.getLastSelectedPathComponent();
	}

	protected boolean canAddRootNodes()
	{
		return canAddRootNodes;
	}

	private void selectTopic(NodeType node)
	{
		if( node != null )
		{
			tree.setSelectionPath(new TreePath(node.getPath()));
		}
		else
		{
			tree.clearSelection();
		}

		tree.updateUI();
		updateActions();
	}
}

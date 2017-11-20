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

package com.tle.common.recipientselector;

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionTreeNode.Grouping;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ExpressionBuilderFinder extends JPanel
	implements
		UserGroupRoleFinder,
		FinderListener,
		TreeSelectionListener
{
	private static final long serialVersionUID = 1L;

	private final RemoteUserService userService;
	private final RecipientFilter[] filters;

	private EventListenerList eventListenerList;
	private boolean selectionMadeInFinder;

	private TabbedFinder finder;
	private ExpressionBuilderModel model;
	private JTree tree;

	public ExpressionBuilderFinder(RemoteUserService userService)
	{
		this(userService, RecipientFilter.USERS, RecipientFilter.GROUPS, RecipientFilter.ROLES,
			RecipientFilter.IP_ADDRESS, RecipientFilter.HOST_REFERRER, RecipientFilter.EXPRESSION);
	}

	public ExpressionBuilderFinder(RemoteUserService userService, RecipientFilter... filters)
	{
		this.userService = userService;
		this.filters = filters;

		setupGUI();
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		tree.setEnabled(b);
		finder.setEnabled(b);
		updateButtons();
	}

	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		// Ignore this
	}

	@Override
	public synchronized void addFinderListener(FinderListener listener)
	{
		if( eventListenerList == null )
		{
			eventListenerList = new EventListenerList();
		}
		eventListenerList.add(FinderListener.class, listener);
	}

	@Override
	public RecipientFilter getSelectedFilter()
	{
		return RecipientFilter.EXPRESSION;
	}

	@Override
	public List<Object> getSelectedResults()
	{
		return Collections.singletonList((Object) getExpression());
	}

	public String getExpression()
	{
		return model.getExpression();
	}

	public void setExpression(String expression)
	{
		model.setExpression(expression);
		updateButtons();
		fireEvent();
	}

	public TreeModel getTreeModel()
	{
		return model;
	}

	@Override
	public void clearAll()
	{
		setExpression(null);
		finder.clearAll();
		updateButtons();
	}

	private void setupGUI()
	{
		JButton addButton = new JButton(addAction);
		JButton removeButton = new JButton(removeAction);
		JButton addGrouping = new JButton(addGroupingAction);

		finder = new TabbedFinder(userService, filters);
		finder.setSingleSelectionOnly(false);
		finder.addFinderListener(this);

		model = new ExpressionBuilderModel();

		tree = new JTree(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isPathEditable(TreePath path)
			{
				return ((ExpressionTreeNode) path.getLastPathComponent()).isGrouping();
			}
		};
		tree.setEditable(true);
		tree.addTreeSelectionListener(this);
		tree.setExpandsSelectedPaths(true);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

		ExpressionTreeCellRenderer cellRenderer = new ExpressionTreeCellRenderer(userService);

		JComboBox<Grouping> editorCombo = new JComboBox<Grouping>();
		editorCombo.addItem(Grouping.MATCH_ANY);
		editorCombo.addItem(Grouping.MATCH_ALL);
		editorCombo.addItem(Grouping.MATCH_NONE);

		DefaultTreeCellEditor cellEditor = new DefaultTreeCellEditor(tree, cellRenderer, new DefaultCellEditor(
			editorCombo));

		tree.setCellRenderer(cellRenderer);
		tree.setCellEditor(cellEditor);

		JScrollPane treeScroller = new JScrollPane(tree);

		setLayout(new MigLayout("fill", // layout constraints
			"[grow 2, fill][][250]", // cols
			"[grow][]10[][grow][]")); // rows

		add(finder, "spany 5");
		add(treeScroller, "skip 1, spany 4, wmin 250px, grow, wrap");
		add(addButton, "wrap, wmax 45px");
		add(removeButton, "wrap, wmax 45px");
		add(addGrouping, "newline, skip 2, center");

		if( CurrentLocale.isRightToLeft() )
		{
			applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			addButton.setText("<");
			removeButton.setText(">");
		}

		updateButtons();
	}

	private void updateButtons()
	{
		addAction.update();
		removeAction.update();
		addGroupingAction.update();
	}

	private ExpressionTreeNode getSelectedTreeNode()
	{
		TreePath selectionPath = tree.getSelectionPath();
		if( selectionPath == null )
		{
			return null;
		}
		return (ExpressionTreeNode) selectionPath.getLastPathComponent();
	}

	private ExpressionTreeNode[] getSelectedTreeNodes()
	{
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if( selectionPaths == null )
		{
			return new ExpressionTreeNode[0];
		}
		ExpressionTreeNode[] results = new ExpressionTreeNode[selectionPaths.length];
		for( int i = 0; i < selectionPaths.length; i++ )
		{
			results[i] = (ExpressionTreeNode) selectionPaths[i].getLastPathComponent();
		}
		return results;
	}

	private ExpressionTreeNode getChildWithExpression(ExpressionTreeNode parent, String expression)
	{
		final int count = parent.getChildCount();
		for( int i = 0; i < count; i++ )
		{
			ExpressionTreeNode child = (ExpressionTreeNode) parent.getChildAt(i);
			if( Objects.equals(child.getExpression(), expression) )
			{
				return child;
			}
		}
		return null;
	}

	private synchronized void fireEvent()
	{
		FinderEvent event = new FinderEvent();
		event.setSource(this);
		event.setSelectionCount(1);

		if( eventListenerList != null )
		{
			for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
			{
				l.valueChanged(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.recipients.FinderListener#valueChanged(com.tle.admin.recipients
	 * .FinderEvent)
	 */
	@Override
	public void valueChanged(FinderEvent e)
	{
		selectionMadeInFinder = e.getSelectionCount() > 0;
		updateButtons();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		updateButtons();
	}

	private void expandChildPathsAndSelectParent(TreeNode parent)
	{
		for( Enumeration<?> en = parent.children(); en.hasMoreElements(); )
		{
			TreeNode child = (TreeNode) en.nextElement();
			tree.expandPath(new TreePath(model.getPathToRoot(child)));
		}

		TreePath parentPath = new TreePath(model.getPathToRoot(parent));
		tree.expandPath(parentPath);
		tree.setSelectionPath(parentPath);
	}

	private final TLEAction addAction = new ShuffleAddAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			RecipientFilter filter = finder.getSelectedFilter();
			List<Object> recipients = finder.getSelectedResults();
			if( recipients.isEmpty() )
			{
				return;
			}

			ExpressionTreeNode newEntity = null;
			ExpressionTreeNode selected = getSelectedTreeNode();

			for( Object recipient : recipients )
			{
				newEntity = addElement(selected, filter, recipient);
			}

			expandChildPathsAndSelectParent(newEntity);
			fireEvent();
		}

		private ExpressionTreeNode addElement(ExpressionTreeNode selected, RecipientFilter filter, Object recipient)
		{
			String newExpression = RecipientUtils.convertToRecipient(filter, recipient);
			ExpressionTreeNode newEntity = new ExpressionTreeNode(newExpression);

			// The following are the parent and child index of the new node
			ExpressionTreeNode parentNode = null;
			int childIndex = -1;

			if( selected == null )
			{
				parentNode = model.getExpressionRoot();
				childIndex = parentNode.getChildCount();
			}
			else if( selected.isGrouping() )
			{
				parentNode = selected;
				childIndex = selected.getChildCount();
			}
			else
			{
				parentNode = (ExpressionTreeNode) selected.getParent();
				childIndex = model.getIndexOfChild(parentNode, selected) + 1;
			}

			ExpressionTreeNode existing = getChildWithExpression(parentNode, newExpression);
			if( existing == null )
			{
				model.insertNodeInto(newEntity, parentNode, childIndex);
			}
			else
			{
				// Don't add another one, just select the existing one.
				newEntity = existing;
			}

			return newEntity;
		}

		@Override
		public void update()
		{
			setEnabled(ExpressionBuilderFinder.this.isEnabled() && selectionMadeInFinder
				&& tree.getSelectionCount() <= 1);
		}
	};

	private final TLEAction removeAction = new ShuffleRemoveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			for( ExpressionTreeNode node : getSelectedTreeNodes() )
			{
				model.removeNodeFromParent(node);
			}
			fireEvent();
		}

		@Override
		public void update()
		{
			ExpressionTreeNode selected = getSelectedTreeNode();
			setEnabled(ExpressionBuilderFinder.this.isEnabled() && selected != null && !selected.isRoot());
		}
	};

	private final TLEAction addGroupingAction = new AddGroupingAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ExpressionTreeNode[] selected = getSelectedTreeNodes();
			ExpressionTreeNode newGroup = new ExpressionTreeNode(Grouping.MATCH_ANY);

			if( selected.length == 1 && selected[0].isGrouping() )
			{
				model.insertNodeInto(newGroup, selected[0], selected[0].getChildCount());
				expandChildPathsAndSelectParent(newGroup);
			}
			else
			{
				ExpressionTreeNode parent = (ExpressionTreeNode) selected[0].getParent();

				int lowestIndex = Integer.MAX_VALUE;
				for( ExpressionTreeNode element : selected )
				{
					lowestIndex = Math.min(lowestIndex, parent.getIndex(element));

					model.removeNodeFromParent(element);
					newGroup.add(element);
				}

				model.insertNodeInto(newGroup, parent, lowestIndex);
				expandChildPathsAndSelectParent(newGroup);
			}
		}

		@Override
		public void update()
		{
			ExpressionTreeNode[] selected = getSelectedTreeNodes();
			if( !ExpressionBuilderFinder.this.isEnabled() || selected.length == 0 )
			{
				setEnabled(false);
			}
			else if( selected.length == 1 )
			{
				setEnabled(true);
			}
			else
			{
				// All selections must be sibilings
				ExpressionTreeNode parent = (ExpressionTreeNode) selected[0].getParent();
				for( int i = 1; i < selected.length; i++ )
				{
					if( selected[i].getParent() != parent )
					{
						setEnabled(false);
						return;
					}
				}
				setEnabled(true);
			}
		}
	};

	private abstract class AddGroupingAction extends AddAction
	{
		public AddGroupingAction()
		{
			putValue(Action.NAME, CurrentLocale.get("com.tle.admin.recipients.expressionbuilderfinder.add"));
			putValue(Action.SHORT_DESCRIPTION,
				CurrentLocale.get("com.tle.admin.recipients.expressionbuilderfinder.add.desc"));
		}
	}
}

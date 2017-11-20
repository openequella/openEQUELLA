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

package com.tle.admin.usermanagement.internal;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.SearchAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.usermanagement.internal.GroupDetailsPanel.MyGlassSwingWorker;
import com.tle.beans.user.GroupTreeNode;
import com.tle.beans.user.TLEGroup;
import com.tle.client.gui.popup.TreePopupListener;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteTLEGroupService;

/**
 * @author Nicholas Read
 */
public class GroupsTab extends JChangeDetectorPanel implements TreeSelectionListener
{
	private static final long serialVersionUID = 1L;

	private final RemoteTLEGroupService groupService;

	private GroupDetailsPanel details;

	private JTextField query;
	JTree tree;
	private DefaultTreeModel model;

	public GroupsTab(ClientService services)
	{
		groupService = services.getService(RemoteTLEGroupService.class);

		setupGui(services);
	}

	private void setupGui(ClientService services)
	{
		query = new JTextField();
		query.setAction(searchAction);

		JButton search = new JButton(searchAction);

		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BorderLayout(5, 5));
		searchPanel.add(query, BorderLayout.CENTER);
		searchPanel.add(search, BorderLayout.EAST);

		model = new DefaultTreeModel(new GroupTreeNode());
		model.setAsksAllowsChildren(true);

		tree = new JTree(model);
		tree.addTreeSelectionListener(this);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addMouseListener(new TreePopupListener(tree, addAction, removeAction));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JButton add = new JButton(addAction);
		JButton remove = new JButton(removeAction);

		details = new GroupDetailsPanel(services, saveAction);

		final int width1 = remove.getPreferredSize().width;
		final int height1 = searchPanel.getPreferredSize().height;
		final int height2 = remove.getPreferredSize().height;

		final int[] rows = {height1, TableLayout.FILL, height2,};
		final int[] cols = {50, width1, width1, 50, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(new JIgnoreChangeComponent(searchPanel), new Rectangle(0, 0, 4, 1));
		add(new JIgnoreChangeComponent(new JScrollPane(tree)), new Rectangle(0, 1, 4, 1));
		add(new JIgnoreChangeComponent(add), new Rectangle(1, 2, 1, 1));
		add(new JIgnoreChangeComponent(remove), new Rectangle(2, 2, 1, 1));
		add(details, new Rectangle(4, 0, 1, 3));

		updateGui();
	}

	private void updateGui()
	{
		removeAction.update();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(final TreeSelectionEvent e)
	{
		if( e.getSource() == tree )
		{
			GlassSwingWorker<TLEGroup> worker = new GlassSwingWorker<TLEGroup>()
			{
				@Override
				public TLEGroup construct()
				{
					GroupTreeNode node = getSelectedGroupNode();
					if( node == null )
					{
						return null;
					}
					else
					{
						return groupService.get(node.getId());
					}
				}

				@Override
				public void finished()
				{
					final TLEGroup g = get();

					details.loadGroup(g, new TreeUpdateName()
					{
						@Override
						public void update(String name)
						{
							updateTreeName(e.getOldLeadSelectionPath(), name);
						}
					});
					updateGui();
				}

				@Override
				public void exception()
				{
					getException().printStackTrace();
				}
			};
			worker.setComponent(this);
			worker.start();
		}
	}

	private GroupTreeNode getSelectedGroupNode()
	{
		TreePath selectionPath = tree.getSelectionPath();
		if( selectionPath != null )
		{
			return (GroupTreeNode) selectionPath.getLastPathComponent();
		}
		else
		{
			return null;
		}
	}

	private final TLEAction removeAction = new RemoveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			boolean performDelete = false;
			final boolean deleteChildren;

			final GroupTreeNode selectedGroupNode = getSelectedGroupNode();
			if( selectedGroupNode.getChildCount() == 0 )
			{
				performDelete = JOptionPane.showConfirmDialog(
					GroupsTab.this,
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.confirm",
						selectedGroupNode.getName())) == JOptionPane.YES_OPTION;
				deleteChildren = false;
			}
			else
			{
				Object[] buttons = new String[]{
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.delete"),
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.keep"),
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.cancel")};
				int result = JOptionPane.showOptionDialog(GroupsTab.this,
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.containssubgroups"),
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.action"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[2]);

				performDelete = result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION;
				deleteChildren = result == JOptionPane.YES_OPTION;
			}

			if( performDelete )
			{
				GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
				{
					@Override
					public Object construct()
					{
						groupService.delete(getSelectedGroupNode().getId(), deleteChildren);
						return null;
					}

					@Override
					public void finished()
					{
						details.clearChanges();
						details.loadGroup(null);
						doSearch();
					}

					@Override
					public void exception()
					{
						Driver.displayInformation(getComponent(),
							CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.error"));
						getException().printStackTrace();
					}
				};
				worker.setComponent(GroupsTab.this);
				worker.start();
			}
		}

		@Override
		public void update()
		{
			setEnabled(tree.getSelectionCount() > 0);
		}
	};

	private final TLEAction addAction = new AddAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			final GroupTreeNode parentGroup = getSelectedGroupNode();

			String prompt = null;
			while( Check.isEmpty(prompt) )
			{
				prompt = JOptionPane.showInputDialog(GroupsTab.this,
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.enter"),
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.new"),
					JOptionPane.QUESTION_MESSAGE);

				if( prompt == null )
				{
					return;
				}
				else if( prompt.trim().length() == 0 )
				{
					JOptionPane.showMessageDialog(GroupsTab.this,
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.notempty"));
				}
			}

			final String newGroupName = prompt;

			GlassSwingWorker<GroupTreeNode> worker = new GlassSwingWorker<GroupTreeNode>()
			{
				@Override
				public GroupTreeNode construct()
				{
					GroupTreeNode result = null;
					if( groupService.getByName(newGroupName) == null )
					{
						result = new GroupTreeNode();
						result.setName(newGroupName);

						String parentId = null;
						if( parentGroup != null )
						{
							parentId = parentGroup.getId();
						}

						result.setId(groupService.add(parentId, newGroupName));
					}
					return result;
				}

				@Override
				public void finished()
				{
					GroupTreeNode group = get();
					if( group == null )
					{
						JOptionPane.showMessageDialog(getComponent(),
							CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.inuse"));
					}
					else
					{
						GroupTreeNode parent = parentGroup;
						if( parent == null )
						{
							parent = (GroupTreeNode) model.getRoot();
						}

						model.insertNodeInto(group, parent, parent.getChildCount());
						tree.setSelectionPath(new TreePath(group.getPath()));
					}
				}

				@Override
				public void exception()
				{
					Driver.displayInformation(getComponent(),
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.errorcreating"));
					getException().printStackTrace();
				}
			};
			worker.setComponent(GroupsTab.this);
			worker.start();
		}
	};

	private final TLEAction saveAction = new SaveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			new MyGlassSwingWorker<String>(GroupsTab.this)
			{
				@Override
				public String doStuff()
				{
					return details.saveLoadedGroup();
				}

				@Override
				public void finished()
				{
					TreePath selectionPath = tree.getSelectionPath();
					if( selectionPath != null )
					{
						GroupTreeNode node = (GroupTreeNode) selectionPath.getLastPathComponent();
						node.setName(get());
						model.nodeChanged(node);
						tree.expandPath(selectionPath);
					}
					JOptionPane.showMessageDialog(getComponent(), "Group saved successfully");
				}
			}.start();
		}

		@Override
		public void update()
		{
			if( details != null )
			{
				details.setEnabled();
			}
		}
	};

	interface TreeUpdateName
	{
		void update(String name);
	}

	void updateTreeName(TreePath selectionPath, String name)
	{
		if( selectionPath != null )
		{
			GroupTreeNode node = (GroupTreeNode) selectionPath.getLastPathComponent();
			node.setName(name);
			model.nodeChanged(node);
			tree.expandPath(selectionPath);
		}
	}

	private final TLEAction searchAction = new SearchAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			doSearch();
		}
	};

	private void doSearch()
	{
		GlassSwingWorker<GroupTreeNode> w = new GlassSwingWorker<GroupTreeNode>()
		{
			@Override
			public GroupTreeNode construct()
			{
				GroupTreeNode results = groupService.searchTree(query.getText());
				results.sortChildren();
				return results;
			}

			@Override
			public void finished()
			{
				GroupTreeNode root = get();
				model.setRoot(root);

				if( root.getChildCount() == 0 )
				{
					JOptionPane.showMessageDialog(getComponent(),
						CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.noresults"));
				}
				else
				{
					Enumeration<?> e = root.depthFirstEnumeration();
					while( e.hasMoreElements() )
					{
						GroupTreeNode node = (GroupTreeNode) e.nextElement();
						if( node.getChildCount() == 0 )
						{
							tree.expandPath(new TreePath(model.getPathToRoot(node)));
						}
					}
				}
			}

			@Override
			public void exception()
			{
				JOptionPane.showMessageDialog(getComponent(),
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupstabs.errorsearching"));
				getException().printStackTrace();
			}
		};
		w.setComponent(this);
		w.start();
	}

	public void save()
	{
		details.saveLoadedGroup();
	}
}

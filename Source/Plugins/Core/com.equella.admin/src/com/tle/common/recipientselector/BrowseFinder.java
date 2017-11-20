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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

public class BrowseFinder extends JPanel implements UserGroupRoleFinder
{
	private static final long serialVersionUID = 1L;
	private final RemoteUserService userService;
	private final boolean allowGroupSelection;
	private final boolean allowUserSelection;

	private TreePanel treePanel;
	private EventListenerList eventListenerList;
	private UserPanel userPanel;
	private BrowsePanelFinder selected;

	public BrowseFinder(RemoteUserService userService, RecipientFilter... filters)
	{
		this.userService = userService;

		List<RecipientFilter> fs = Arrays.asList(filters);
		allowGroupSelection = fs.contains(RecipientFilter.GROUPS);
		allowUserSelection = fs.contains(RecipientFilter.USERS);

		treePanel = new TreePanel();
		selected = treePanel;

		if( !allowUserSelection )
		{
			setLayout(new GridLayout(1, 1));
			add(treePanel);
		}
		else
		{
			userPanel = new UserPanel();
			selected = userPanel;

			setLayout(new GridLayout(2, 1));
			add(treePanel);
			add(userPanel);
		}
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		treePanel.setEnabled(b);
		userPanel.setEnabled(b);
	}

	private class Mouse extends MouseAdapter
	{
		private final BrowsePanelFinder finder;

		public Mouse(BrowsePanelFinder finder)
		{
			this.finder = finder;
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			selected = finder;
		}
	}

	List<UserBean> search(String query)
	{
		try
		{
			List<UserBean> rv = userService.searchUsers(query, treePanel.getSelectedParentGroup(), true);
			Collections.sort(rv, Format.USER_BEAN_COMPARATOR);
			return rv;
		}
		catch( RuntimeException ex )
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	private BrowsePanelFinder getSelectedPanel()
	{
		return selected;
	}

	private BrowsePanelFinder[] getPanels()
	{
		return new BrowsePanelFinder[]{treePanel, userPanel};
	}

	@Override
	public RecipientFilter getSelectedFilter()
	{
		return getSelectedPanel().getSelectedFilter();
	}

	@Override
	public List<Object> getSelectedResults()
	{
		return getSelectedPanel().getSelectedResults();
	}

	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		for( BrowsePanelFinder finder : getPanels() )
		{
			finder.setSingleSelectionOnly(b);
		}
	}

	@Override
	public void clearAll()
	{
		for( BrowsePanelFinder finder : getPanels() )
		{
			finder.clearAll();
		}

		getSelectedPanel().fireEvent();
	}

	@Override
	public void addFinderListener(FinderListener listener)
	{
		if( eventListenerList == null )
		{
			eventListenerList = new EventListenerList();
		}
		eventListenerList.add(FinderListener.class, listener);
	}

	private abstract class BrowsePanelFinder extends JPanel implements UserGroupRoleFinder
	{
		protected Mouse mouse;

		public BrowsePanelFinder()
		{
			mouse = new Mouse(this);
		}

		@Override
		public void addFinderListener(FinderListener listener)
		{
			// Ignore
		}

		public void fireEvent()
		{
			if( eventListenerList != null )
			{
				FinderEvent event = new FinderEvent();
				event.setSource(this);
				event.setSelectionCount(getSelectedResultCount());

				for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
				{
					l.valueChanged(event);
				}
			}
		}

		protected abstract int getSelectedResultCount();
	}

	@SuppressWarnings("nls")
	private class UserPanel extends BrowsePanelFinder implements ListSelectionListener, ActionListener
	{
		private JTextField query;
		private JButton search;

		private GenericListModel<Object> resultsModel;
		private JList results;

		public UserPanel()
		{
			resultsModel = new GenericListModel<Object>();
			results = new JList(resultsModel);
			results.addListSelectionListener(this);

			query = new JTextField();
			query.addActionListener(this);
			search = new JButton(CurrentLocale.get("searching.userGroupRole.executeQuery"));
			search.addActionListener(this);
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(5, 5));
			panel.add(new JLabel(CurrentLocale.get("com.tle.admin.recipients.browserfinder.users")), BorderLayout.WEST);
			panel.add(query, BorderLayout.CENTER);
			panel.add(search, BorderLayout.EAST);

			query.addMouseListener(mouse);
			search.addMouseListener(mouse);
			results.addMouseListener(mouse);

			setLayout(new BorderLayout(5, 5));

			add(panel, BorderLayout.NORTH);
			add(new JScrollPane(results), BorderLayout.CENTER);
			setBorder(new EmptyBorder(5, 5, 5, 5));
		}

		@Override
		public void setEnabled(boolean b)
		{
			super.setEnabled(b);

			query.setEnabled(b);
			search.setEnabled(b);
			results.setEnabled(b);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			GlassSwingWorker<List<UserBean>> w = new GlassSwingWorker<List<UserBean>>()
			{
				@Override
				public List<UserBean> construct() throws Exception
				{
					return search(query.getText());
				}

				@Override
				public void finished()
				{
					resultsModel.clear();
					resultsModel.addAll(get());

					if( resultsModel.isEmpty() )
					{
						JOptionPane.showMessageDialog(UserPanel.this,
							CurrentLocale.get("com.tle.admin.recipients.browserfinder.noresults"));
					}
				}
			};
			w.setComponent(BrowseFinder.this);
			w.start();
		}

		@Override
		protected int getSelectedResultCount()
		{
			return results.getSelectedIndices().length;
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			fireEvent();
		}

		@Override
		public RecipientFilter getSelectedFilter()
		{
			return RecipientFilter.USERS;
		}

		@Override
		public List<Object> getSelectedResults()
		{
			return Arrays.asList(results.getSelectedValues());
		}

		@Override
		public void setSingleSelectionOnly(boolean b)
		{
			int mode = b ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
			results.getSelectionModel().setSelectionMode(mode);
		}

		@Override
		public void clearAll()
		{
			query.setText(null);
			resultsModel.clear();
		}
	}

	@SuppressWarnings("nls")
	private class TreePanel extends BrowsePanelFinder implements TreeSelectionListener, ActionListener
	{
		private JTextField query;
		private JButton search;

		private JTree tree;
		private DefaultTreeModel model;

		public TreePanel()
		{
			query = new JTextField();
			query.addActionListener(this);

			search = new JButton(CurrentLocale.get("searching.userGroupRole.executeQuery"));
			search.addActionListener(this);

			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(5, 5));
			panel
				.add(new JLabel(CurrentLocale.get("com.tle.admin.recipients.browserfinder.groups")), BorderLayout.WEST);
			panel.add(query, BorderLayout.CENTER);
			panel.add(search, BorderLayout.EAST);

			model = new DefaultTreeModel(null);
			tree = new JTree(model);

			setLayout(new BorderLayout(5, 5));
			add(panel, BorderLayout.NORTH);
			add(new JScrollPane(tree), BorderLayout.CENTER);

			tree.addMouseListener(mouse);
			tree.setShowsRootHandles(true);
			tree.setRootVisible(false);
			tree.addTreeSelectionListener(this);
		}

		@Override
		public void setEnabled(boolean b)
		{
			super.setEnabled(b);

			query.setEnabled(b);
			search.setEnabled(b);
			tree.setEnabled(b);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			GlassSwingWorker<GroupNode> w = new GlassSwingWorker<GroupNode>()
			{
				private Map<String, GroupNode> cachedNodes = new HashMap<String, GroupNode>();
				private GroupNode root = new RootNode();

				@Override
				public GroupNode construct() throws Exception
				{
					List<GroupBean> searchResults = userService.searchGroups(query.getText());
					for( GroupBean gb : searchResults )
					{
						setupParents(new GroupNode(gb));
					}
					return null;
				}

				private GroupNode setupParents(GroupNode node)
				{
					if( !cachedNodes.containsKey(node.get().getUniqueID()) )
					{
						GroupBean parent = userService.getParentGroupForGroup(node.getId());
						if( parent == null )
						{
							root.insertInOrder(node);
						}
						else
						{
							GroupNode pnode = new GroupNode(parent);
							pnode = setupParents(pnode);
							pnode.insertInOrder(node);
						}

						cachedNodes.put(node.getId(), node);
					}
					return cachedNodes.get(node.getId());
				}

				@Override
				public void finished()
				{
					if( root.getChildCount() == 0 )
					{
						JOptionPane.showMessageDialog(TreePanel.this,
							CurrentLocale.get("com.tle.admin.recipients.browserfinder.noresults"));
						model.setRoot(null);
					}
					else
					{
						model.setRoot(root);

						Enumeration<?> e = root.depthFirstEnumeration();
						while( e.hasMoreElements() )
						{
							TreeNode node = (TreeNode) e.nextElement();
							if( node.getChildCount() == 0 )
							{
								tree.expandPath(new TreePath(model.getPathToRoot(node)));
							}
						}
					}
				}

			};
			w.setComponent(BrowseFinder.this);
			w.start();
		}

		public String getSelectedParentGroup()
		{
			TreePath selectionPath = tree.getSelectionPath();
			if( selectionPath != null )
			{
				return ((GroupNode) selectionPath.getLastPathComponent()).getId();
			}
			else
			{
				return null;
			}
		}

		@Override
		public RecipientFilter getSelectedFilter()
		{
			return RecipientFilter.GROUPS;
		}

		@Override
		public List<Object> getSelectedResults()
		{
			TreePath[] paths = tree.getSelectionPaths();
			List<Object> o = new ArrayList<Object>(paths.length);
			for( TreePath path : paths )
			{
				Object o2 = ((GroupNode) path.getLastPathComponent()).get();
				if( o2 != null )
				{
					o.add(o2);
				}
			}
			return o;
		}

		@Override
		public void setSingleSelectionOnly(boolean b)
		{
			int mode = b ? TreeSelectionModel.SINGLE_TREE_SELECTION : TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
			tree.getSelectionModel().setSelectionMode(mode);
		}

		@Override
		public void clearAll()
		{
			query.setText("");
			model.setRoot(null);
			tree.clearSelection();
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			if( allowGroupSelection )
			{
				fireEvent();
			}
		}

		@Override
		protected int getSelectedResultCount()
		{
			return tree.getSelectionCount();
		}

		protected class RootNode extends GroupNode
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getId()
			{
				return null;
			}
		}

		protected class GroupNode extends DefaultMutableTreeNode
		{
			private static final long serialVersionUID = 1L;

			public GroupNode()
			{
				super();
			}

			public GroupNode(GroupBean group)
			{
				super(group);
			}

			public GroupBean get()
			{
				return (GroupBean) getUserObject();
			}

			public String getId()
			{
				return get().getUniqueID();
			}

			@SuppressWarnings("unchecked")
			public void insertInOrder(MutableTreeNode newChild)
			{
				if( !Check.isEmpty(children) )
				{
					List<MutableTreeNode> mtns = new ArrayList<MutableTreeNode>(children);
					mtns.add(newChild);
					Collections.sort(mtns, new NumberStringComparator<MutableTreeNode>());

					removeAllChildren();
					for( MutableTreeNode mtn : mtns )
					{
						add(mtn);
					}
				}
				else
				{
					add(newChild);
				}
			}

			@Override
			public boolean equals(Object obj)
			{
				if( obj instanceof GroupNode )
				{
					GroupNode rhs = (GroupNode) obj;
					return Objects.equals(get(), rhs.get());
				}
				return false;
			}

			@Override
			public int hashCode()
			{
				return get() == null ? 0 : get().hashCode();
			}
		}
	}
}
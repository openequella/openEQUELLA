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

import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.Format;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class SearchFinder extends JPanel implements ActionListener, UserGroupRoleFinder, ListSelectionListener
{
	private static final long serialVersionUID = 1L;

	private final RemoteUserService userService;

	private EventListenerList eventListenerList;

	private JTextField query;
	private JButton search;
	private JRadioButton users;
	private JRadioButton groups;
	private JRadioButton roles;
	private JList results;
	private GenericListModel<Object> resultsModel;

	public SearchFinder(RemoteUserService userService, RecipientFilter... filters)
	{
		if( filters.length == 0 )
		{
			throw new IllegalArgumentException(CurrentLocale.get("com.tle.admin.recipients.searchfinder.onefilter"));
		}

		this.userService = userService;

		setupGUI(filters);
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		query.setEnabled(b);
		search.setEnabled(b);
		results.setEnabled(b);

		if( users != null )
		{
			users.setEnabled(b);
		}

		if( groups != null )
		{
			groups.setEnabled(b);
		}

		if( roles != null )
		{
			roles.setEnabled(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.recipients.UserGroupRoleFinder#setSingleSelectionOnly(boolean
	 * )
	 */
	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		int mode = b ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		results.getSelectionModel().setSelectionMode(mode);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.recipients.UserGroupRoleFinder#addFinderListener(com.tle
	 * .admin.recipients.FinderListener)
	 */
	@Override
	public synchronized void addFinderListener(FinderListener listener)
	{
		if( eventListenerList == null )
		{
			eventListenerList = new EventListenerList();
		}
		eventListenerList.add(FinderListener.class, listener);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.gui.searching.UserGroupRoleSelector#getSelectedFilter()
	 */
	@Override
	public RecipientFilter getSelectedFilter()
	{
		if( users != null && users.isSelected() )
		{
			return RecipientFilter.USERS;
		}
		else if( groups != null && groups.isSelected() )
		{
			return RecipientFilter.GROUPS;
		}
		else if( roles != null && roles.isSelected() )
		{
			return RecipientFilter.ROLES;
		}
		else
		{
			throw new RuntimeException("We have reached an illegal state");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.gui.searching.UserGroupRoleSelector#getSelectedResults()
	 */
	@Override
	public List<Object> getSelectedResults()
	{
		return Arrays.asList(results.getSelectedValues());
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.recipients.UserGroupRoleFinder#clearAll()
	 */
	@Override
	public void clearAll()
	{
		query.setText("");
		resultsModel.clear();
	}

	private void setupGUI(RecipientFilter... filters)
	{
		JLabel queryText = new JLabel(CurrentLocale.get("searching.userGroupRole.beforeQuery"));
		JLabel filterText = new JLabel(CurrentLocale.get("searching.userGroupRole.beforeFilters"));
		JLabel resultsText = new JLabel(CurrentLocale.get("searching.userGroupRole.results"));

		queryText.setHorizontalAlignment(SwingConstants.RIGHT);
		filterText.setHorizontalAlignment(SwingConstants.RIGHT);
		resultsText.setHorizontalAlignment(SwingConstants.RIGHT);

		JComponent filterPanel = createFilterPanel(filters);

		query = new JTextField();
		query.addActionListener(this);

		search = new JButton(CurrentLocale.get("searching.userGroupRole.executeQuery"));
		search.addActionListener(this);

		resultsModel = new GenericListModel<Object>();
		results = new JList(resultsModel);
		results.addListSelectionListener(this);
		setSingleSelectionOnly(false);

		JScrollPane resultsScroller = new JScrollPane(results);
		resultsScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		final int height1 = query.getPreferredSize().height;
		final int height2 = filterPanel == null ? TableLayout.INVISIBLE : filterPanel.getPreferredSize().height;
		final int height3 = resultsText.getPreferredSize().height;
		final int width1 = queryText.getPreferredSize().width;
		final int width2 = search.getPreferredSize().width;

		final int[] rows = {height1, height2, height3, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.FILL, width2,};

		setLayout(new TableLayout(rows, cols));
		add(queryText, new Rectangle(0, 0, 1, 1));
		add(query, new Rectangle(1, 0, 1, 1));
		add(search, new Rectangle(2, 0, 1, 1));
		if( filterPanel != null )
		{
			add(filterText, new Rectangle(0, 1, 1, 1));
			add(filterPanel, new Rectangle(1, 1, 2, 1));
		}
		add(resultsText, new Rectangle(0, 2, 1, 1));
		add(resultsScroller, new Rectangle(1, 2, 2, 2));
	}

	private JComponent createFilterPanel(RecipientFilter... filters)
	{
		ButtonGroup group = new ButtonGroup();
		for( RecipientFilter filter : filters )
		{
			switch( filter )
			{
				case USERS:
					users = createFilterButton(group, "searching.userGroupRole.filter.users");
					break;

				case GROUPS:
					groups = createFilterButton(group, "searching.userGroupRole.filter.groups");
					break;

				case ROLES:
					roles = createFilterButton(group, "searching.userGroupRole.filter.roles");
					break;

				default:
					break;
			}
		}

		if( filters.length == 1 )
		{
			// We don't need to show this panel
			return null;
		}
		else
		{
			// Add each of the buttons to the panel
			JPanel all = new JPanel(new FlowLayout(FlowLayout.LEFT));
			for( Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements(); )
			{
				all.add(e.nextElement());
			}
			return all;
		}
	}

	private JRadioButton createFilterButton(ButtonGroup group, String key)
	{
		JRadioButton button = new JRadioButton(CurrentLocale.get(key));
		group.add(button);
		if( group.getButtonCount() == 1 )
		{
			button.setSelected(true);
		}
		button.addActionListener(this);
		return button;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public synchronized void valueChanged(ListSelectionEvent e)
	{
		if( eventListenerList != null )
		{
			FinderEvent event = new FinderEvent();
			event.setSource(this);
			event.setSelectionCount(results.getSelectedIndices().length);

			for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
			{
				l.valueChanged(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == users || e.getSource() == groups || e.getSource() == roles )
		{
			resultsModel.clear();
		}
		else if( e.getSource() == query || e.getSource() == search )
		{
			resultsModel.clear();

			ResultsWorker<?> worker = null;
			if( users != null && users.isSelected() )
			{
				worker = new ResultsWorker<UserBean>()
				{
					@Override
					public List<UserBean> doSearch(String query)
					{
						return userService.searchUsers(query);
					}

					@Override
					public Comparator<? super UserBean> getComparator()
					{
						return Format.USER_BEAN_COMPARATOR;
					}
				};
			}
			else if( groups != null && groups.isSelected() )
			{
				worker = new ResultsWorker<GroupBean>()
				{
					@Override
					public List<GroupBean> doSearch(String query)
					{
						return userService.searchGroups(query);
					}

					@Override
					public Comparator<? super GroupBean> getComparator()
					{
						return Format.GROUP_BEAN_COMPARATOR;
					}
				};
			}
			else if( roles != null && roles.isSelected() )
			{
				worker = new ResultsWorker<RoleBean>()
				{
					@Override
					public List<RoleBean> doSearch(String query)
					{
						return userService.searchRoles(query);
					}

					@Override
					public Comparator<? super RoleBean> getComparator()
					{
						return Format.ROLE_BEAN_COMPARATOR;
					}
				};
			}

			if( worker != null )
			{
				worker.setComponent(this);
				worker.start();
			}
		}
	}

	protected abstract class ResultsWorker<T> extends GlassSwingWorker<List<T>>
	{
		public abstract List<T> doSearch(String query);

		@Override
		public List<T> construct() throws Exception
		{
			List<T> resultList = doSearch(query.getText().trim());
			Collections.sort(resultList, getComparator());
			return resultList;
		}

		public abstract Comparator<? super T> getComparator();

		@Override
		public void finished()
		{
			List<T> list = get();
			if( list.isEmpty() )
			{
				JOptionPane.showMessageDialog(SearchFinder.this,
					CurrentLocale.get("com.tle.admin.recipients.searchfinder.noresults"));
			}
			else
			{
				resultsModel.addAll(list);
			}
		}

		@Override
		public void exception()
		{
			JOptionPane.showMessageDialog(getComponent(), "Error attempting to search", "Error",
				JOptionPane.WARNING_MESSAGE);
		}
	}
}

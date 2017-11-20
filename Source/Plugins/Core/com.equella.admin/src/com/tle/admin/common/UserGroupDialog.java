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

package com.tle.admin.common;

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

public class UserGroupDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private JButton okButton;
	private UserGroupPanel mainPanel;
	private boolean okPressed = false;

	public UserGroupDialog(Dialog parent, RemoteUserService userService)
	{
		super(parent);
		createGui(userService);
	}

	@Override
	public void setVisible(boolean b)
	{
		if( b )
		{
			okPressed = false;
		}
		super.setVisible(b);
	}

	public boolean isOK()
	{
		return okPressed;
	}

	public boolean isGroupSelection()
	{
		return mainPanel.tabs.getSelectedIndex() == 1;
	}

	public boolean isUserSelection()
	{
		return mainPanel.tabs.getSelectedIndex() == 0;
	}

	public NameValue getResult()
	{
		return mainPanel.getResult();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		okPressed = e.getSource() == okButton;
		dispose();
	}

	@SuppressWarnings("nls")
	private void createGui(RemoteUserService userService)
	{
		setTitle(CurrentLocale.get("com.dytech.edge.admin.helper.usergroupdialog.title"));

		okButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.ok"));
		JButton cancelButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.cancel"));

		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		mainPanel = new UserGroupPanel(userService);

		int height1 = cancelButton.getPreferredSize().height;
		int width1 = cancelButton.getPreferredSize().width;
		int[] rows = {TableLayout.FILL, height1};
		int[] cols = {TableLayout.FILL, width1, width1};

		JPanel content = new JPanel(new TableLayout(rows, cols));
		content.add(mainPanel, new Rectangle(0, 0, 3, 1));
		content.add(okButton, new Rectangle(1, 1, 1, 1));
		content.add(cancelButton, new Rectangle(2, 1, 1, 1));

		setContentPane(content);
	}

	private static class UserGroupPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 1L;

		private static final Log LOGGER = LogFactory.getLog(UserGroupPanel.class);

		private final RemoteUserService userService;

		private JTextField userQuery;
		private JTextField groupQuery;
		private JButton userSearch;
		private JButton groupSearch;

		private JList userList;
		private JList groupList;
		private DefaultListModel userModel;
		private DefaultListModel groupModel;

		JTabbedPane tabs;

		public UserGroupPanel(RemoteUserService userService)
		{
			this.userService = userService;
			createGui();
		}

		public NameValue getResult()
		{
			if( tabs.getSelectedIndex() == 0 ) // user
			{
				return (NameValue) userList.getSelectedValue();
			}
			return (NameValue) groupList.getSelectedValue();
		}

		private void doUserSearch(String query)
		{
			userModel.removeAllElements();
			try
			{
				List<UserBean> results = userService.searchUsers(query);
				Collections.sort(results, Format.USER_BEAN_COMPARATOR);
				for( UserBean user : results )
				{
					userModel.addElement(UserBeanUtils.formatUser(user));
				}
			}
			catch( Exception ex )
			{
				Driver.displayError(getParent(), "usersAndGroups/searchingUsers", ex); //$NON-NLS-1$
				LOGGER.warn("Problem searching users with " + query, ex);
			}
		}

		private void doGroupSearch(String query)
		{
			groupModel.removeAllElements();
			try
			{
				List<GroupBean> results = userService.searchGroups(query);
				Collections.sort(results, Format.GROUP_BEAN_COMPARATOR);
				for( GroupBean group : results )
				{
					groupModel.addElement(UserBeanUtils.formatGroup(group));
				}
			}
			catch( Exception ex )
			{
				Driver.displayError(getParent(), "usersAndGroups/listingGroups", ex); //$NON-NLS-1$
				LOGGER.warn("Problem searching groups with " + query, ex);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if( source.equals(userSearch) || source.equals(userQuery) )
			{
				doUserSearch(userQuery.getText());
			}
			else if( source.equals(groupSearch) || source.equals(groupQuery) )
			{
				doGroupSearch(groupQuery.getText());
			}
		}

		@SuppressWarnings("nls")
		private void createGui()
		{
			userQuery = new JTextField();
			groupQuery = new JTextField();

			final String searchText = CurrentLocale.get("com.dytech.edge.admin.helper.usergroupdialog.search");
			userSearch = new JButton(searchText);
			groupSearch = new JButton(searchText);

			userQuery.addActionListener(this);
			groupQuery.addActionListener(this);
			userSearch.addActionListener(this);
			groupSearch.addActionListener(this);

			userModel = new DefaultListModel();
			groupModel = new DefaultListModel();

			userList = new JList(userModel);
			groupList = new JList(groupModel);

			tabs = new JTabbedPane();
			tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.helper.usergroupdialog.selectuser"),
				createTab("user", userQuery, userSearch, userList));
			tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.helper.usergroupdialog.selectgroup"),
				createTab("group", groupQuery, groupSearch, groupList));

			setLayout(new GridLayout(1, 1));
			add(tabs);
		}

		private JPanel createTab(String subject, JTextField field, JButton button, JList list)
		{
			JLabel instruction = new JLabel(CurrentLocale.get(
				"com.dytech.edge.admin.helper.usergroupdialog.selectsomething", //$NON-NLS-1$
				subject));

			JScrollPane scrollPane = new JScrollPane(list);

			final int height1 = instruction.getPreferredSize().height;
			final int height2 = button.getPreferredSize().height;
			final int width1 = button.getPreferredSize().width;

			final int[] rows = {height1, height2, TableLayout.FILL};
			final int[] cols = {TableLayout.FILL, width1};

			JPanel all = new JPanel(new TableLayout(rows, cols));
			all.setBorder(AppletGuiUtils.DEFAULT_BORDER);

			all.add(instruction, new Rectangle(0, 0, 2, 1));
			all.add(field, new Rectangle(0, 1, 1, 1));
			all.add(button, new Rectangle(1, 1, 1, 1));
			all.add(scrollPane, new Rectangle(0, 2, 2, 1));

			return all;
		}
	}
}

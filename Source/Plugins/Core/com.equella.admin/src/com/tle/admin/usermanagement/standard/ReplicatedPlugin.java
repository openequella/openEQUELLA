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

package com.tle.admin.usermanagement.standard;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.admin.helper.JdbcDriver;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.plugin.HelpListener;
import com.tle.beans.usermanagement.standard.ReplicatedConfiguration;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class ReplicatedPlugin extends GeneralPlugin<ReplicatedConfiguration> implements ItemListener
{
	private static final String SQL_PRESET_STANDARD = "standard";
	private static final String SQL_PRESET_CUSTOM = "custom";

	private JTextField username;
	private JPasswordField password;
	JTextField url;
	JComboBox drivers;

	private static final String HELP_TEXT = "ump.replicated.";
	private static final String HELP_AUTHENTICATE = HELP_TEXT + "authenticate";
	private static final String HELP_USERINFO = HELP_TEXT + "user.info";
	private static final String HELP_USERROLE = HELP_TEXT + "user.role";
	private static final String HELP_GROUPINFO = HELP_TEXT + "group.info";
	private static final String HELP_SEARCHUSERS = HELP_TEXT + "search.users";
	private static final String HELP_SEARCHGROUPS = HELP_TEXT + "search.groups";
	private static final String HELP_SEARCH_GROUPS_IN_GROUP = HELP_TEXT + "search.groups.in.group";
	private static final String HELP_SEARCH_GROUPS_IN_GROUP_RECURSIVE = HELP_TEXT + "search.groups.in.group.recursive";
	private static final String HELP_USERS_IN_GROUP_RECURSIVE = HELP_TEXT + "users.in.group.recursive";
	private static final String HELP_USERS_IN_GROUP = HELP_TEXT + "users.in.group";
	private static final String HELP_SEARCH_USERS_IN_GROUP_RECURSIVE = HELP_TEXT + "search.users.in.group.recursive";
	private static final String HELP_SEARCH_USERS_IN_GROUP = HELP_TEXT + "search.users.in.group";
	private static final String HELP_GROUPS_CONTAINING_USER = HELP_TEXT + "groups.containing.user";
	private static final String HELP_DIGEST = HELP_TEXT + "digest";
	private static final String HELP_PARENT_GROUP = HELP_TEXT + "parent.group";
	private static final String HELP_SEARCH_ROLES = HELP_TEXT + "search.roles";
	private static final String HELP_ROLES_INFO = HELP_TEXT + "roles.info";

	private JComboBox presets;
	private JComboBox digests;
	private RSyntaxTextArea authenticate;
	private RSyntaxTextArea userInfo;
	private RSyntaxTextArea userRole;
	private RSyntaxTextArea searchRoles;
	private RSyntaxTextArea roleInfo;
	private RSyntaxTextArea groupInfo;
	private RSyntaxTextArea searchUsers;
	private RSyntaxTextArea searchGroups;
	private RSyntaxTextArea searchGroupsInGroup;
	private RSyntaxTextArea searchGroupsInGroupRecursive;
	private RSyntaxTextArea usersInGroupRecursive;
	private RSyntaxTextArea usersInGroup;
	private RSyntaxTextArea searchUsersInGroupRecursive;
	private RSyntaxTextArea searchUsersInGroup;
	private RSyntaxTextArea groupsContainingUser;
	private RSyntaxTextArea parentGroup;

	protected JPanel all;
	private int row;
	private int width;

	public ReplicatedPlugin()
	{
		setup();
	}

	private RSyntaxTextArea createSyntaxTextField()
	{
		RSyntaxTextArea area = new RSyntaxTextArea();
		area.setRows(3);
		area.setLineWrap(true);
		area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
		area.setHighlightCurrentLine(false);
		return area;
	}

	private void setup()
	{
		JTabbedPane tabs = new JTabbedPane();
		username = new JTextField();
		password = new JPasswordField();
		url = new JTextField();

		drivers = new JComboBox();
		JdbcDriver.addDrivers(drivers);
		drivers.addItemListener(this);

		JNameValuePanel p = new JNameValuePanel();
		p.addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.driver"), drivers);
		p.addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.jdbcurl"), url);
		p.addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.username"), username);
		p.addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.password"), password);

		p.getComponent().setBorder(new EmptyBorder(5, 5, 5, 5));
		tabs.addTab(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.details"),
			new JScrollPane(p.getComponent()));

		presets = new JComboBox(getTypes());
		presets.addItemListener(this);

		digests = new JComboBox(ReplicatedConfiguration.DIGESTS.toArray());
		digests.setEditable(true);

		JLabel presetLabel = new JLabel(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.presets"));

		all = new JPanel();
		row = -1;

		authenticate = createSyntaxTextField();
		userInfo = createSyntaxTextField();
		groupInfo = createSyntaxTextField();
		searchUsers = createSyntaxTextField();
		searchGroups = createSyntaxTextField();
		searchGroupsInGroup = createSyntaxTextField();
		searchGroupsInGroupRecursive = createSyntaxTextField();
		usersInGroupRecursive = createSyntaxTextField();
		usersInGroup = createSyntaxTextField();
		searchUsersInGroup = createSyntaxTextField();
		searchUsersInGroupRecursive = createSyntaxTextField();
		groupsContainingUser = createSyntaxTextField();
		userRole = createSyntaxTextField();
		roleInfo = createSyntaxTextField();
		searchRoles = createSyntaxTextField();
		parentGroup = createSyntaxTextField();

		int pref = TableLayout.PREFERRED;
		int[] cols = new int[]{width, TableLayout.FILL};
		all.setLayout(new TableLayout(new int[]{pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref,
				pref, pref, pref, pref, pref, pref}, cols));

		all.add(presetLabel, new Rectangle(0, ++row, 1, 1));
		all.add(presets, new Rectangle(1, row, 1, 1));

		add(digests, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.digest"), HELP_DIGEST);
		add(authenticate, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.authenticate"),
			HELP_AUTHENTICATE);
		add(userInfo, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.userinfo"), HELP_USERINFO);
		add(userRole, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.userrole"), HELP_USERROLE);
		add(roleInfo, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.roleinfo"), HELP_ROLES_INFO);
		add(searchRoles, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchroles"),
			HELP_SEARCH_ROLES);
		add(groupInfo, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.groupinfo"), HELP_GROUPINFO);
		add(searchUsers, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchusers"),
			HELP_SEARCHUSERS);
		add(searchGroups, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchgroups"),
			HELP_SEARCHGROUPS);
		add(searchGroupsInGroup,
			CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchgroupsingroup"),
			HELP_SEARCH_GROUPS_IN_GROUP);
		add(searchGroupsInGroupRecursive,
			CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchgroupsingroup.recursive"),
			HELP_SEARCH_GROUPS_IN_GROUP_RECURSIVE);
		add(usersInGroupRecursive, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.usersinr"),
			HELP_USERS_IN_GROUP_RECURSIVE);
		add(usersInGroup, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.usersin"),
			HELP_USERS_IN_GROUP);
		add(searchUsersInGroupRecursive,
			CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchusersinr"),
			HELP_SEARCH_USERS_IN_GROUP_RECURSIVE);
		add(searchUsersInGroup, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.searchusersin"),
			HELP_SEARCH_USERS_IN_GROUP);

		add(groupsContainingUser, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.groupcontaining"),
			HELP_GROUPS_CONTAINING_USER);
		add(parentGroup, CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.parentgroup"),
			HELP_PARENT_GROUP);

		cols[0] = width;
		all.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabs.addTab(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.sql"), new JScrollPane(all));
		addFillComponent(tabs);
	}

	private void add(JComponent comp, String text, String help)
	{
		JLabel label = new JLabel(text);
		all.add(label, new Rectangle(0, ++row, 1, 1));
		all.add(comp, new Rectangle(1, row, 1, 1));

		width = Math.max(width, label.getPreferredSize().width);

		label.addMouseListener(new HelpListener(this.getComponent(), text, help)
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				all.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				all.setCursor(Cursor.getDefaultCursor());
			}

		});
	}

	private void add(JTextComponent comp, String text, String help)
	{
		JScrollPane scroll = new JScrollPane(comp);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll, text, help);
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if( e.getSource() == presets )
		{
			NameValue selection = (NameValue) presets.getSelectedItem();
			String value = selection.getValue();
			if( value.equals(SQL_PRESET_STANDARD) )
			{
				loadSql(new ReplicatedConfiguration.TleConfiguration());
			}
			else if( value.equals(SQL_PRESET_CUSTOM) )
			{
				loadSql(new ReplicatedConfiguration.EmptyConfiguration());
			}
		}
		else if( e.getSource() == drivers )
		{
			if( e.getStateChange() != ItemEvent.SELECTED )
			{
				return;
			}

			if( !Check.isEmpty(url.getText())
				&& JOptionPane.showConfirmDialog(getComponent(),
					CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.confirmjdbcurlreset")) != JOptionPane.YES_OPTION )
			{
				return;
			}

			JdbcDriver driver = (JdbcDriver) drivers.getSelectedItem();
			url.setText(driver.getJdbcUrl());
		}
	}

	@Override
	public boolean save(ReplicatedConfiguration config)
	{
		config.setDigest((String) digests.getSelectedItem());

		config.setAuthenticate(authenticate.getText());
		config.setUserInfo(userInfo.getText());
		config.setUserRoles(userRole.getText());
		config.setGroupInfo(groupInfo.getText());
		config.setSearchUsers(searchUsers.getText());
		config.setSearchGroups(searchGroups.getText());
		config.setSearchGroupsInGroup(searchGroupsInGroup.getText());
		config.setSearchGroupsInGroupRecursive(searchGroupsInGroupRecursive.getText());
		config.setUsersInGroupRecursive(usersInGroupRecursive.getText());
		config.setUsersInGroup(usersInGroup.getText());
		config.setSearchUsersInGroupRecursive(searchUsersInGroupRecursive.getText());
		config.setSearchUsersInGroup(searchUsersInGroup.getText());
		config.setGroupsContainingUser(groupsContainingUser.getText());
		config.setParentGroup(parentGroup.getText());
		config.setRoleInfo(roleInfo.getText());
		config.setSearchRoles(searchRoles.getText());

		config.setUrl(url.getText());
		config.setUsername(username.getText());
		config.setPassword(new String(password.getPassword()));
		config.setDriver(((JdbcDriver) drivers.getSelectedItem()).getDriverClass());

		return true;
	}

	@Override
	public void load(ReplicatedConfiguration config)
	{
		loadSql(config);

		String driverClass = config.getDriver();
		for( JdbcDriver d : JdbcDriver.getDrivers() )
		{
			if( d.getDriverClass().equals(driverClass) )
			{
				drivers.setSelectedItem(d);
			}
		}

		username.setText(config.getUsername());
		password.setText(config.getPassword());
		url.setText(config.getUrl());

		if( Check.isEmpty(url.getText()) )
		{
			url.setText(((JdbcDriver) drivers.getSelectedItem()).getJdbcUrl());
		}
	}

	private void loadSql(ReplicatedConfiguration config)
	{
		authenticate.setText(config.getAuthenticate());
		userInfo.setText(config.getUserInfo());
		groupInfo.setText(config.getGroupInfo());
		searchUsers.setText(config.getSearchUsers());
		searchGroups.setText(config.getSearchGroups());
		searchGroupsInGroup.setText(config.getSearchGroupsInGroup());
		searchGroupsInGroupRecursive.setText(config.getSearchGroupsInGroupRecursive());
		usersInGroupRecursive.setText(config.getUsersInGroupRecursive());
		usersInGroup.setText(config.getUsersInGroup());
		searchUsersInGroupRecursive.setText(config.getSearchUsersInGroupRecursive());
		searchUsersInGroup.setText(config.getSearchUsersInGroup());
		groupsContainingUser.setText(config.getGroupsContainingUser());
		parentGroup.setText(config.getParentGroup());
		userRole.setText(config.getUserRoles());
		searchRoles.setText(config.getSearchRoles());
		roleInfo.setText(config.getRoleInfo());
		digests.setSelectedItem(config.getDigest());
	}

	@Override
	public void validation()
	{
		// There is nothing to validate at the moment.
	}

	private static NameValue[] getTypes()
	{
		return new NameValue[]{
				new NameValue("", ""),
				new NameValue(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.standard"),
					SQL_PRESET_STANDARD),
				new NameValue(CurrentLocale.get("com.tle.admin.usermanagement.replicatedplugin.custom"),
					SQL_PRESET_CUSTOM)};
	}
}

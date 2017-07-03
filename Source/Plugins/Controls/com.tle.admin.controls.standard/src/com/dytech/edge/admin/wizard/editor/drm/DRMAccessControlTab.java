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

package com.dytech.edge.admin.wizard.editor.drm;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DRMPage.Container;
import com.dytech.edge.wizard.beans.DRMPage.Contributor;
import com.dytech.gui.TableLayout;
import com.dytech.gui.calendar.CalendarDialog;
import com.dytech.gui.filter.FilteredShuffleList;
import com.tle.admin.Driver;
import com.tle.admin.common.FilterGroupModel;
import com.tle.admin.common.FilterUserModel;
import com.tle.admin.helper.Network;
import com.tle.admin.helper.NetworkShuffleList;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.JGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class DRMAccessControlTab extends JPanel
{
	private static final long serialVersionUID = 1L;

	private UserGroupPanel usersGroups;
	private NetworkPanel network;
	private AccessCountPanel access;
	private DatePanel dates;

	public DRMAccessControlTab(RemoteUserService userService)
	{
		usersGroups = new UserGroupPanel(userService);
		network = new NetworkPanel();
		access = new AccessCountPanel();
		dates = new DatePanel();

		final int height1 = usersGroups.getPreferredSize().height;
		final int height2 = network.getPreferredSize().height;
		final int height3 = access.getPreferredSize().height;
		final int height4 = dates.getPreferredSize().height;
		final int[] rows = {height1, height2, height3, height4, TableLayout.FILL};
		final int[] cols = {TableLayout.FILL};

		setLayout(new TableLayout(rows, cols, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(usersGroups, new Rectangle(0, 0, 1, 1));
		add(network, new Rectangle(0, 1, 1, 1));
		add(access, new Rectangle(0, 2, 1, 1));
		add(dates, new Rectangle(0, 3, 1, 1));
	}

	public void load(DRMPage page)
	{
		Contributor contributor = page.getContributor();
		Container container = page.getContainer();
		{
			RemoteUserService userService = Driver.instance().getClientService().getService(RemoteUserService.class);
			List<String> userIds = Collections.emptyList();
			List<String> groupIds = Collections.emptyList();
			if( !contributor.getUsers().isEmpty() || !contributor.getGroups().isEmpty() )
			{
				usersGroups.setSelectableByContributor(true);
				userIds = contributor.getUsers();
				groupIds = contributor.getGroups();
			}
			else
			{
				userIds = container.getUsers();
				groupIds = container.getGroups();
			}
			Collection<NameValue> users = new ArrayList<NameValue>();
			Collection<NameValue> groups = new ArrayList<NameValue>();

			for( String userId : userIds )
			{
				users.add(UserBeanUtils.getUser(userService, userId));
			}
			for( String groupId : groupIds )
			{
				groups.add(UserBeanUtils.getGroup(userService, groupId));
			}
			if( users.size() > 0 || groups.size() > 0 )
			{
				usersGroups.addUsersAndGroups(users, groups);
			}
		}

		{
			Set<com.dytech.edge.wizard.beans.DRMPage.Network> networks;
			if( !contributor.getNetworks().isEmpty() )
			{
				network.setSelectableByContributor(true);
				networks = contributor.getNetworks();
			}
			else
			{
				networks = container.getNetworks();
			}

			network.removeAllNetworks();
			if( networks != null )
			{
				for( com.dytech.edge.wizard.beans.DRMPage.Network pagenetwork : networks )
				{
					Network nw = new Network();
					nw.setName(pagenetwork.getName());
					nw.setMin(pagenetwork.getMin());
					nw.setMax(pagenetwork.getMax());

					network.addNetwork(nw);
				}
			}
		}

		if( container.getCount() != 0 )
		{
			access.setFixedCount(container.getCount());
		}
		else if( contributor.isCount() )
		{
			if( contributor.getCount() == 0 )
			{
				access.setSelectableByContributor();
			}
			else
			{
				access.setDefaultCount(contributor.getCount());
			}
		}

		if( container.getAcceptStart() != null )
		{
			dates.setFixedDateRange(container.getAcceptStart(), container.getAcceptEnd());
		}
		else if( contributor.isDatetime() )
		{
			dates.setSelectableByContributor();
		}
	}

	public void save(DRMPage page)
	{
		Contributor contributor = page.getContributor();
		Container container = page.getContainer();
		if( usersGroups.isSelected() )
		{
			List<String> users = new ArrayList<String>();
			List<String> groups = new ArrayList<String>();
			for( NameValue user : usersGroups.getUsers() )
			{
				users.add(user.getValue());
			}

			for( NameValue group : usersGroups.getGroups() )
			{
				groups.add(group.getValue());
			}
			if( usersGroups.isSelectableByContributor() )
			{
				contributor.setUsers(users);
				contributor.setGroups(groups);
			}
			else
			{
				container.setUsers(users);
				container.setGroups(groups);
			}

		}

		if( network.isSelected() )
		{
			Set<com.dytech.edge.wizard.beans.DRMPage.Network> networks = new HashSet<com.dytech.edge.wizard.beans.DRMPage.Network>();
			final int count = network.getNetworkCount();
			for( int i = 0; i < count; i++ )
			{
				Network nw = network.getNetworkAt(i);
				com.dytech.edge.wizard.beans.DRMPage.Network newnw = new com.dytech.edge.wizard.beans.DRMPage.Network();
				newnw.setMin(nw.getMin());
				newnw.setMax(nw.getMax());
				newnw.setName(nw.getName());
				networks.add(newnw);
			}

			if( network.isSelectableByContributor() )
			{
				contributor.getNetworks().addAll(networks);
			}
			else
			{
				container.getNetworks().addAll(networks);
			}

		}

		if( access.isSelected() )
		{
			if( access.isFixedCount() )
			{
				container.setCount(access.getCount());
			}
			else if( access.isSelectableByContributor() )
			{
				contributor.setIsCount(true);
			}
			else if( access.isDefaultCount() )
			{
				contributor.setCount(access.getCount());
			}
		}

		if( dates.isFixedDateRange() )
		{
			container.setAcceptStart(dates.getStartDate());
			container.setAcceptEnd(dates.getEndDate());
		}
		else if( dates.isSelectableByContributor() )
		{
			contributor.setDatetime(true);
		}
	}

	private static final class UserGroupPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final JCheckBox selectable;
		private final FilteredShuffleList<NameValue> userList;
		private final FilteredShuffleList<NameValue> groupList;
		private final JLabel userLabel;
		private final JLabel groupLabel;

		public UserGroupPanel(RemoteUserService userService)
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrict"), //$NON-NLS-1$
				false);

			userLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.users")); //$NON-NLS-1$
			groupLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.groups")); //$NON-NLS-1$

			userList = new FilteredShuffleList<NameValue>(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrictusers"), //$NON-NLS-1$
				new FilterUserModel(userService));
			groupList = new FilteredShuffleList<NameValue>(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrictgroups"), //$NON-NLS-1$
				new FilterGroupModel(userService));

			selectable = new JCheckBox(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.selectable")); //$NON-NLS-1$

			final int height1 = userLabel.getMinimumSize().height;
			final int height2 = userList.getMinimumSize().height;
			final int height3 = selectable.getPreferredSize().height;
			final int[] rows = {height1, height2, height1, height2, height3};
			final int[] cols = {TableLayout.FILL};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(userLabel, new Rectangle(0, 0, 1, 1));
			addInner(userList, new Rectangle(0, 1, 1, 1));
			addInner(groupLabel, new Rectangle(0, 2, 1, 1));
			addInner(groupList, new Rectangle(0, 3, 1, 1));
			addInner(selectable, new Rectangle(0, 4, 1, 1));

			setSelected(false);
		}

		public void setSelectableByContributor(boolean b)
		{
			selectable.setSelected(b);
		}

		public boolean isSelectableByContributor()
		{
			return selectable.isSelected();
		}

		public List<NameValue> getUsers()
		{
			return userList.getItems();
		}

		public List<NameValue> getGroups()
		{
			return groupList.getItems();
		}

		public void addUsersAndGroups(Collection<NameValue> users, Collection<NameValue> groups)
		{
			userList.removeAllItems();
			groupList.removeAllItems();

			userList.addItems(users);
			groupList.addItems(groups);

			setSelected(true);
		}
	}

	private static final class NetworkPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final NetworkShuffleList networks;
		private final JCheckBox selectable;
		private final JLabel titleLabel;

		public NetworkPanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrictip"), //$NON-NLS-1$
				false);

			titleLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.ipbased")); //$NON-NLS-1$
			selectable = new JCheckBox(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.selectable")); //$NON-NLS-1$
			networks = new NetworkShuffleList();

			final int height1 = titleLabel.getMinimumSize().height;
			final int height2 = networks.getMinimumSize().height;
			final int height3 = selectable.getPreferredSize().height;
			final int[] rows = {height1, height2, height3};
			final int[] cols = {TableLayout.FILL};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(titleLabel, new Rectangle(0, 0, 1, 1));
			addInner(networks, new Rectangle(0, 1, 1, 1));
			addInner(selectable, new Rectangle(0, 2, 1, 1));

			setSelected(false);
		}

		public void setSelectableByContributor(boolean b)
		{
			selectable.setSelected(b);
		}

		public boolean isSelectableByContributor()
		{
			return selectable.isSelected();
		}

		public int getNetworkCount()
		{
			return networks.getNetworkCount();
		}

		public Network getNetworkAt(int index)
		{
			return networks.getNetworkAt(index);
		}

		public void removeAllNetworks()
		{
			networks.removeAllNetworks();
		}

		public void addNetwork(Network n)
		{
			networks.addNetwork(n);
			setSelected(true);
		}
	}

	private static final class AccessCountPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final JRadioButton selectable;
		private final JRadioButton fixed;
		private final JRadioButton initial;
		private final JSpinner accesses;
		private final SpinnerNumberModel accessesModel;

		public AccessCountPanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrictaccesses"), //$NON-NLS-1$
				false);

			JLabel accessesLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.number")); //$NON-NLS-1$

			selectable = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.numberspecified"), //$NON-NLS-1$
				true);
			fixed = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.specifyfixed")); //$NON-NLS-1$
			initial = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.specifydefault")); //$NON-NLS-1$

			ButtonGroup group = new ButtonGroup();
			group.add(selectable);
			group.add(fixed);
			group.add(initial);

			selectable.addActionListener(this);
			fixed.addActionListener(this);
			initial.addActionListener(this);

			accessesModel = new SpinnerNumberModel(100, 0, 1000000, 1);
			accesses = new JSpinner(accessesModel);

			final int height1 = selectable.getPreferredSize().height;
			final int height2 = accesses.getPreferredSize().height;
			final int width1 = accessesLabel.getPreferredSize().width;
			final int width2 = accesses.getPreferredSize().width;
			final int[] rows = {height1, height1, height1, height2};
			final int[] cols = {15, width1, width2, TableLayout.FILL};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(selectable, new Rectangle(0, 0, 4, 1));
			addInner(fixed, new Rectangle(0, 1, 4, 1));
			addInner(initial, new Rectangle(0, 2, 4, 1));
			addInner(accessesLabel, new Rectangle(1, 3, 1, 1));
			addInner(accesses, new Rectangle(2, 3, 1, 1));

			setSelected(false);
		}

		public boolean isSelectableByContributor()
		{
			return isSelected() && selectable.isSelected();
		}

		public boolean isFixedCount()
		{
			return isSelected() && fixed.isSelected();
		}

		public boolean isDefaultCount()
		{
			return isSelected() && initial.isSelected();
		}

		public void setSelectableByContributor()
		{
			selectable.setSelected(true);
			setSelected(true);
		}

		public void setFixedCount(int number)
		{
			accessesModel.setValue(number);
			fixed.setSelected(true);
			setSelected(true);
		}

		public void setDefaultCount(int number)
		{
			accessesModel.setValue(number);
			initial.setSelected(true);
			setSelected(true);
		}

		public int getCount()
		{
			return accessesModel.getNumber().intValue();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( e.getSource() == selectable || e.getSource() == fixed || e.getSource() == initial )
			{
				update();
			}
			else
			{
				super.actionPerformed(e);
			}
		}

		@Override
		public void setSelected(boolean enabled)
		{
			super.setSelected(enabled);
			update();
		}

		private void update()
		{
			accesses.setEnabled(isSelected() && !selectable.isSelected());
		}
	}

	private final class DatePanel extends JGroup
	{
		private static final long serialVersionUID = 1L;

		private final DateFormat DATE_PANEL_FORMAT = new SimpleDateFormat("dd MMM yyyy");

		private final JRadioButton fixed;
		private final JRadioButton selectable;
		private final JTextField startField;
		private final JTextField endField;
		private final JButton startSelect;
		private final JButton endSelect;
		private Date startDate;
		private Date endDate;

		public DatePanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.restrictdate"), //$NON-NLS-1$
				false);

			JLabel startLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.start")); //$NON-NLS-1$
			JLabel endLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.end")); //$NON-NLS-1$

			endLabel.setHorizontalTextPosition(SwingConstants.RIGHT);

			fixed = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.specifyvalid"), //$NON-NLS-1$
				true);
			selectable = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.daterange")); //$NON-NLS-1$

			ButtonGroup group = new ButtonGroup();
			group.add(fixed);
			group.add(selectable);

			fixed.addActionListener(this);
			selectable.addActionListener(this);

			startField = new JTextField();
			endField = new JTextField();

			startField.setEditable(false);
			endField.setEditable(false);

			startSelect = new JButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.select")); //$NON-NLS-1$
			endSelect = new JButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.select")); //$NON-NLS-1$

			startSelect.addActionListener(this);
			endSelect.addActionListener(this);

			final int height1 = fixed.getPreferredSize().height;
			final int height2 = startField.getPreferredSize().height;
			final int width1 = startLabel.getPreferredSize().width;
			final int width2 = startSelect.getPreferredSize().width;
			final int[] rows = {height1, height2, height2, height1,};
			final int[] cols = {15, width1, width2 * 3, width2,};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(fixed, new Rectangle(0, 0, 4, 1));
			addInner(startLabel, new Rectangle(1, 1, 1, 1));
			addInner(startField, new Rectangle(2, 1, 1, 1));
			addInner(startSelect, new Rectangle(3, 1, 1, 1));
			addInner(endLabel, new Rectangle(1, 2, 1, 1));
			addInner(endField, new Rectangle(2, 2, 1, 1));
			addInner(endSelect, new Rectangle(3, 2, 1, 1));
			addInner(selectable, new Rectangle(0, 3, 4, 1));

			setSelected(false);
		}

		public boolean isSelectableByContributor()
		{
			return isSelected() && selectable.isSelected();
		}

		public boolean isFixedDateRange()
		{
			return isSelected() && fixed.isSelected();
		}

		public void setSelectableByContributor()
		{
			selectable.setSelected(true);
			setSelected(true);
		}

		public Date getStartDate()
		{
			return startDate;
		}

		public Date getEndDate()
		{
			return endDate;
		}

		public void setFixedDateRange(Date start, Date end)
		{
			startDate = start;
			endDate = end;

			if( startDate == null )
			{
				startDate = new Date();
			}

			if( endDate == null )
			{
				endDate = new Date();
			}

			if( endDate.before(startDate) )
			{
				endDate = (Date) startDate.clone();
			}

			startField.setText(DATE_PANEL_FORMAT.format(startDate));
			endField.setText(DATE_PANEL_FORMAT.format(endDate));

			setSelected(true);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( e.getSource() == selectable || e.getSource() == fixed )
			{
				update();
			}
			else if( e.getSource() == startSelect )
			{
				Date d = CalendarDialog.showCalendarDialog(DRMAccessControlTab.this,
					CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.selectdate"), //$NON-NLS-1$
					startDate);
				if( d != null )
				{
					setFixedDateRange(d, endDate);
				}
			}
			else if( e.getSource() == endSelect )
			{
				Date d = CalendarDialog.showCalendarDialog(DRMAccessControlTab.this,
					CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmacccesscontroltab.selectdate"), //$NON-NLS-1$
					startDate);
				if( d != null )
				{
					setFixedDateRange(startDate, d);
				}
			}
			else
			{
				super.actionPerformed(e);
			}
		}

		@Override
		public void setSelected(boolean enabled)
		{
			super.setSelected(enabled);
			update();
		}

		private void update()
		{
			boolean enable = isSelected() && fixed.isSelected();
			startSelect.setEnabled(enable);
			endSelect.setEnabled(enable);
		}
	}
}

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

import java.awt.Component;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.JSmartTextField;
import com.dytech.gui.TableLayout;
import com.dytech.gui.filter.FilteredShuffleList;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.helper.FilterUserBeanModel;
import com.tle.admin.usermanagement.internal.GroupsTab.TreeUpdateName;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class GroupDetailsPanel extends JChangeDetectorPanel
{
	private static final long serialVersionUID = 1L;
	private final RemoteTLEGroupService groupService;
	private final RemoteUserService userService;
	private final TLEAction saveAction;

	private transient TLEGroup loadedGroup;

	private ChangeDetector changeDetector;
	private JTextField groupId;
	private JTextField groupName;
	private JTextArea description;
	private FilteredShuffleList<UserBean> users;

	public GroupDetailsPanel(ClientService services, TLEAction saveAction)
	{
		this.saveAction = saveAction;
		this.groupService = services.getService(RemoteTLEGroupService.class);
		this.userService = services.getService(RemoteUserService.class);

		setupGui();

		loadGroup(null);
	}

	private void setupGui()
	{
		JLabel groupIdLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.groupid")); //$NON-NLS-1$
		JLabel groupNameLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.groupname")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.desc")); //$NON-NLS-1$
		JLabel usersLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.members")); //$NON-NLS-1$

		descriptionLabel.setVerticalTextPosition(SwingConstants.TOP);
		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);

		groupId = new JTextField();
		groupId.setEditable(false);
		groupName = new JSmartTextField(100);
		description = new JTextArea();
		description.setLineWrap(true);
		description.setWrapStyleWord(true);

		users = new FilteredShuffleList<UserBean>(
			CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.search"), //$NON-NLS-1$
			new FilterUserBeanModel(userService), Format.USER_BEAN_COMPARATOR);
		users.setSearchText(CurrentLocale
			.get("com.tle.admin.usermanagement.standard.internal.groupdetailspanel.searchbutton")); //$NON-NLS-1$
		users.setRemoveText(CurrentLocale
			.get("com.tle.admin.usermanagement.standard.internal.groupdetailspanel.removebutton")); //$NON-NLS-1$

		JButton save = new JButton(saveAction);

		final int height1 = groupName.getPreferredSize().height;
		final int height2 = save.getPreferredSize().height;
		final int width1 = groupNameLabel.getPreferredSize().width;
		final int width2 = save.getPreferredSize().width;

		final int[] rows = {height1, height1, TableLayout.FILL, height1, TableLayout.TRIPLE_FILL, height2,};
		final int[] cols = {width1, TableLayout.FILL, width2,};

		setLayout(new TableLayout(rows, cols));

		add(groupIdLabel, new Rectangle(0, 0, 1, 1));
		add(groupId, new Rectangle(1, 0, 2, 1));
		add(groupNameLabel, new Rectangle(0, 1, 1, 1));
		add(groupName, new Rectangle(1, 1, 2, 1));
		add(descriptionLabel, new Rectangle(0, 2, 1, 1));
		add(new JScrollPane(description), new Rectangle(1, 2, 2, 1));

		add(usersLabel, new Rectangle(0, 3, 3, 1));
		add(users, new Rectangle(0, 4, 3, 1));
		add(save, new Rectangle(2, 5, 1, 1));

		changeDetector = new ChangeDetector();
		changeDetector.watch(groupName);
		changeDetector.watch(description);
		changeDetector.watch(users.getModel());

		saveAction.setEnabled(false);
	}

	public void loadGroup(final TLEGroup group)
	{
		loadGroup(group, null);
	}

	public void loadGroup(final TLEGroup group, final TreeUpdateName r)
	{
		if( loadedGroup != null && group != null && loadedGroup.getId() == group.getId() )
		{
			return;
		}

		if( !changeDetector.hasDetectedChanges() )
		{
			loadDetails(group);
		}
		else
		{
			Object[] buttons = new Object[]{
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.save"), //$NON-NLS-1$
					CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.dontsave")}; //$NON-NLS-1$
			int results = JOptionPane.showOptionDialog(this,
				CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.savemods"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.usermanagement.internal.groupdetailspanel.savegroup"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[1]);

			if( results == JOptionPane.NO_OPTION )
			{
				loadDetails(group);
			}
			else if( results == JOptionPane.YES_OPTION )
			{
				new MyGlassSwingWorker<String>(this)
				{
					@Override
					public String doStuff()
					{
						return saveLoadedGroup();
					}

					@Override
					public void finished()
					{
						if( r != null && loadedGroup != null )
						{
							r.update(loadedGroup.getName());
						}
						loadDetails(group);
					}
				}.start();
			}
		}
	}

	protected void setEnabled()
	{
		setEnabled(loadedGroup != null);
	}

	private void loadDetails(TLEGroup group)
	{
		loadedGroup = group;

		boolean enabled = loadedGroup != null;
		groupName.setEnabled(enabled);
		description.setEnabled(enabled);
		users.setEnabled(enabled);

		saveAction.update();

		users.removeAllItems();

		if( enabled )
		{
			groupId.setText(Check.nullToEmpty(group.getUuid()));
			groupName.setText(Check.nullToEmpty(group.getName()));
			description.setText(Check.nullToEmpty(group.getDescription()));

			GlassSwingWorker<Collection<UserBean>> worker = new GlassSwingWorker<Collection<UserBean>>()
			{
				@Override
				public Collection<UserBean> construct()
				{
					return userService.getInformationForUsers(loadedGroup.getUsers()).values();
				}

				@Override
				public void finished()
				{
					if( get() != null )
					{
						users.addItems(get());
					}

					clearChanges();
				}

				@Override
				public void exception()
				{
					Exception ex = getException();
					ex.printStackTrace();
					Driver.displayError(GroupDetailsPanel.this, "unknown", ex); //$NON-NLS-1$

					clearChanges();
				}
			};
			worker.setComponent(this);
			worker.start();
		}
		else
		{
			groupId.setText("");
			groupName.setText("");
			clearChanges();
		}
	}

	private void saveDetails()
	{
		loadedGroup.setName(groupName.getText());
		loadedGroup.setDescription(description.getText());

		Set<String> userIds = new HashSet<String>();
		for( UserBean user : users.getItems() )
		{
			if( user != null )
			{
				userIds.add(user.getUniqueID());
			}
		}
		loadedGroup.setUsers(userIds);
	}

	public String saveLoadedGroup()
	{
		saveDetails();

		String id = groupService.edit(loadedGroup);
		loadedGroup = groupService.get(id);

		clearChanges();
		return loadedGroup.getName();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
		super.clearChanges();
	}

	/**
	 * @author Nicholas Read
	 */
	protected abstract static class MyGlassSwingWorker<T> extends GlassSwingWorker<T>
	{
		public abstract T doStuff();

		public MyGlassSwingWorker(Component c)
		{
			setComponent(c);
		}

		@Override
		public T construct()
		{
			return doStuff();
		}

		@Override
		public void exception()
		{
			Exception ex = getException();
			if( ex instanceof InvalidDataException )
			{
				InvalidDataException ide = (InvalidDataException) ex;
				ValidationError error = ide.getErrors().get(0);
				JOptionPane.showMessageDialog(getComponent(), error.getMessage());
			}
			else
			{
				Driver.displayError(getComponent(), "unknown", ex); //$NON-NLS-1$
			}
		}
	}
}

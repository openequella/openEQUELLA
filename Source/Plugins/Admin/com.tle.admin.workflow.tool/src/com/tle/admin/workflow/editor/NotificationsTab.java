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

package com.tle.admin.workflow.editor;

import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.getRecipientType;
import static com.tle.common.security.SecurityConstants.getRecipientValue;
import static com.tle.common.security.SecurityConstants.Recipient.GROUP;
import static com.tle.common.security.SecurityConstants.Recipient.USER;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.helper.GroupBox;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.MultipleFinderControl;
import com.tle.common.recipientselector.RecipientFilter;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.core.remoting.RemoteUserService;

public class NotificationsTab extends JPanel
{
	private static final long serialVersionUID = 1L;
	private GroupBox notifyOnCompletionGroupBox;

	private MultipleFinderControl notifyOnCompletionFinder;
	private MultipleFinderControl notifyOnErrorFinder;
	private static String keyPfx = "com.tle.admin.workflow.editor.scripteditor.nofificationstab.";

	public NotificationsTab(ChangeDetector changeDetector, RemoteUserService userService)
	{
		setupGui(userService);
		setupChangeDetector(changeDetector);
	}

	private void setupGui(RemoteUserService userService)
	{
		notifyOnCompletionFinder = new MultipleFinderControl(userService, RecipientFilter.USERS, RecipientFilter.GROUPS);
		notifyOnCompletionGroupBox = GroupBox.withCheckBox(CurrentLocale.get(keyPfx + "completed.groupbox"), false);
		notifyOnCompletionGroupBox.getInnerPanel().setLayout(new GridLayout(1, 1));
		notifyOnCompletionGroupBox.add(notifyOnCompletionFinder);

		notifyOnErrorFinder = new MultipleFinderControl(userService, RecipientFilter.USERS, RecipientFilter.GROUPS);

		final int[] rows = {TableLayout.FILL, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};
		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		JPanel errorPanel = new JPanel();
		JLabel errorLabel = new JLabel(CurrentLocale.get(keyPfx + "error.label"));
		errorPanel.setLayout(new TableLayout(new int[]{errorLabel.getPreferredSize().height, TableLayout.FILL}, new int[] {TableLayout.FILL}));
		errorPanel.add(errorLabel, new Rectangle(0,0,1,1));
		errorPanel.add(notifyOnErrorFinder, new Rectangle(0, 1, 1,1));
		add(errorPanel, new Rectangle(0, 0, 1, 1));
		add(notifyOnCompletionGroupBox, new Rectangle(0, 1, 1, 1));
	}

	private void setupChangeDetector(ChangeDetector changeDetector)
	{
		notifyOnCompletionFinder.watch(changeDetector);
		notifyOnErrorFinder.watch(changeDetector);
	}

	public void load(ScriptNode node)
	{
		notifyOnCompletionGroupBox.setSelected(node.isNotifyOnCompletion());

		List<String> notifyOnCompletionList = new ArrayList<String>();
		if( node.getUsersNotifyOnCompletion() != null )
		{
			for( String user : node.getUsersNotifyOnCompletion() )
			{
				notifyOnCompletionList.add(getRecipient(USER, user));
			}
		}

		if( node.getGroupsNotifyOnCompletion() != null )
		{
			for( String grp : node.getGroupsNotifyOnCompletion() )
			{
				notifyOnCompletionList.add(getRecipient(GROUP, grp));
			}
		}

		notifyOnCompletionFinder.load(notifyOnCompletionList);

		List<String> notifyOnErrorList = new ArrayList<String>();
		if( node.getUsersNotifyOnError() != null )
		{
			for( String user : node.getUsersNotifyOnError() )
			{
				notifyOnErrorList.add(getRecipient(USER, user));
			}
		}

		if( node.getGroupsNotifyOnError() != null )
		{
			for( String grp : node.getGroupsNotifyOnError() )
			{
				notifyOnErrorList.add(getRecipient(GROUP, grp));
			}
		}

		notifyOnErrorFinder.load(notifyOnErrorList);

	}

	public void save(ScriptNode node)
	{
		node.setUsersNotifyOnCompletion(null);
		node.setGroupsNotifyOnCompletion(null);
		node.setUsersNotifyOnError(null);
		node.setGroupsNotifyOnError(null);
		node.setNotifyOnCompletion(false);

		if( notifyOnCompletionGroupBox.isSelected() )
		{
			Set<String> users = new HashSet<String>();
			Set<String> groups = new HashSet<String>();

			for( String result : notifyOnCompletionFinder.save() )
			{
				String value = getRecipientValue(result);
				switch( getRecipientType(result) )
				{
					case USER:
						users.add(value);
						break;
					case GROUP:
						groups.add(value);
						break;
					default:
						throw new IllegalStateException("We should never reach here");
				}
			}

			if( users.isEmpty() )
			{
				users = null;
			}

			if( groups.isEmpty() )
			{
				groups = null;
			}

			node.setNotifyOnCompletion(true);
			node.setUsersNotifyOnCompletion(users);
			node.setGroupsNotifyOnCompletion(groups);
		}

		Set<String> users = new HashSet<String>();
		Set<String> groups = new HashSet<String>();

		for( String result : notifyOnErrorFinder.save() )
		{
			String value = getRecipientValue(result);
			switch( getRecipientType(result) )
			{
				case USER:
					users.add(value);
					break;
				case GROUP:
					groups.add(value);
					break;
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}

		if( users.isEmpty() )
		{
			users = null;
		}

		if( groups.isEmpty() )
		{
			groups = null;
		}

		node.setUsersNotifyOnError(users);
		node.setGroupsNotifyOnError(groups);
	}
}

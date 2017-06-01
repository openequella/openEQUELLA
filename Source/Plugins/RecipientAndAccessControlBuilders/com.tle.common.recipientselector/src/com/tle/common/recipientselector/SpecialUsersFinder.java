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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.EventListenerList;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class SpecialUsersFinder extends JPanel implements UserGroupRoleFinder
{
	private static final long serialVersionUID = 1L;

	private final RemoteUserService userService;

	private EventListenerList eventListenerList;
	private JRadioButton everyone;
	private JRadioButton owner;
	private JRadioButton loggedIn;
	private JRadioButton guest;
	private JRadioButton sharedSecretId;
	private JComboBox sharedSecretIds;

	public SpecialUsersFinder(RemoteUserService userService, boolean hasOwner)
	{
		this.userService = userService;

		setupGUI(hasOwner);
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		everyone.setEnabled(b);
		owner.setEnabled(b);
		loggedIn.setEnabled(b);
		guest.setEnabled(b);
		sharedSecretId.setEnabled(b);
		sharedSecretIds.setEnabled(b && sharedSecretIds.getItemCount() > 0);
	}

	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		// Ignore
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
		Object value = null;
		if( everyone.isSelected() )
		{
			value = SecurityConstants.getRecipient(Recipient.EVERYONE);
		}
		else if( owner.isSelected() )
		{
			value = SecurityConstants.getRecipient(Recipient.OWNER);
		}
		else if( loggedIn.isSelected() )
		{
			value = SecurityConstants.getRecipient(Recipient.ROLE, SecurityConstants.LOGGED_IN_USER_ROLE_ID);
		}
		else if( guest.isSelected() )
		{
			value = SecurityConstants.getRecipient(Recipient.ROLE, SecurityConstants.GUEST_USER_ROLE_ID);
		}
		else if( sharedSecretId.isSelected() )
		{
			value = SecurityConstants.getRecipient(Recipient.TOKEN_SECRET_ID,
				(String) sharedSecretIds.getSelectedItem());
		}
		else
		{
			throw new IllegalStateException();
		}
		return Arrays.asList(value);
	}

	@Override
	public void clearAll()
	{
		everyone.setSelected(true);
		sharedSecretIds.setEnabled(false);
		fireEvent();
	}

	private void setupGUI(boolean hasOwner)
	{
		everyone = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.specialusersfinder.everyone"), true);
		owner = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.specialusersfinder.owner"));
		loggedIn = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.specialusersfinder.loggedin"));
		guest = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.specialusersfinder.guest"));
		sharedSecretId = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.specialusersfinder.tokenId"));

		sharedSecretIds = new JComboBox();
		sharedSecretIds.setEnabled(false);
		sharedSecretId.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean enable = sharedSecretId.isSelected();
				boolean empty = sharedSecretIds.getItemCount() == 0;

				if( enable && empty )
				{
					GlassSwingWorker<List<String>> worker = new GlassSwingWorker<List<String>>()
					{
						@Override
						public List<String> construct() throws Exception
						{
							return userService.getTokenSecretIds();
						}

						@Override
						public void finished()
						{
							AppletGuiUtils.addItemsToJCombo(sharedSecretIds, get());
							sharedSecretIds.setEnabled(true);
						}
					};
					worker.setComponent(SpecialUsersFinder.this);
					worker.start();
				}
				else
				{
					sharedSecretIds.setEnabled(enable);
				}
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(everyone);
		group.add(owner);
		group.add(loggedIn);
		group.add(guest);
		group.add(sharedSecretId);

		final int height1 = sharedSecretIds.getPreferredSize().height;

		final int[] rows = {height1, height1, height1, height1, height1, height1, TableLayout.FILL,};
		final int[] cols = {15, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		int row = -1;
		add(everyone, new Rectangle(0, ++row, 2, 1));
		if( hasOwner )
		{
			add(owner, new Rectangle(0, ++row, 2, 1));
		}
		add(loggedIn, new Rectangle(0, ++row, 2, 1));
		add(guest, new Rectangle(0, ++row, 2, 1));

		add(sharedSecretId, new Rectangle(0, ++row, 2, 1));
		add(sharedSecretIds, new Rectangle(1, ++row, 1, 1));
	}

	private synchronized void fireEvent()
	{
		FinderEvent event = new FinderEvent();
		event.setSource(this);
		event.setSelectionCount(1);

		for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
		{
			l.valueChanged(event);
		}
	}
}

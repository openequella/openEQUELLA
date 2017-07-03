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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import com.tle.common.Format;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

@SuppressWarnings("nls")
public class SingleUserSelector extends JPanel implements ActionListener
{
	private final JTextField userField;
	private final JButton searchForUser;
	private UserBean selectedUser;
	private final RemoteUserService userService;

	public SingleUserSelector(RemoteUserService userService)
	{
		this(userService, null);
	}

	public SingleUserSelector(RemoteUserService userService, String text)
	{
		this.userService = userService;

		userField = new JTextField();
		userField.setEditable(false);

		searchForUser = new JButton(CurrentLocale.get("com.dytech.edge.admin.gui.common.singleuserselector.search"));
		searchForUser.addActionListener(this);

		setLayout(new MigLayout("fill, insets 0"));
		if( text != null )
		{
			add(new JLabel(text));
		}
		add(userField, "growx, pushx");
		add(searchForUser);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		SingleFinderDialog ugd = new SingleFinderDialog(userService, RecipientFilter.USERS);
		Pair<RecipientFilter, Object> result = ugd.showFinder(this.getParent());

		if( result != null )
		{
			setUser((UserBean) result.getSecond());
		}
	}

	public UserBean getUser()
	{
		return selectedUser;
	}

	public void setUser(UserBean user)
	{
		String string = ""; //$NON-NLS-1$
		selectedUser = user;
		if( user != null )
		{
			string = Format.format(user);
		}
		userField.setText(string);
	}

	public void setUserId(String userId)
	{
		if( userId == null )
		{
			setUser(null);
		}
		else
		{
			setUser(userService.getInformationForUser(userId));
		}
	}

	public JTextComponent getUserField()
	{
		return userField;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		searchForUser.setEnabled(enabled);
	}
}

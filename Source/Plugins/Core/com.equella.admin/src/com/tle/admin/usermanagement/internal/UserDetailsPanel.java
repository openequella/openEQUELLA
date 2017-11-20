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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteTLEUserService;
import com.tle.core.remoting.RemoteUserService;

/*
 * @author Nicholas Read
 */
public class UserDetailsPanel extends JChangeDetectorPanel
{
	private static final long serialVersionUID = 1L;

	private final RemoteTLEUserService userService;
	private final RemoteUserService userCacheService;

	private final Map<String, JLabel> labels = new HashMap<String, JLabel>();

	private transient TLEUser loadedUser;

	private ChangeDetector changeDetector;
	private JTextField identifier;
	private JTextField username;
	private JTextField firstName;
	private JTextField lastName;
	private JTextField emailAddress;
	private JPasswordField newPassword;
	private JPasswordField passwordConfirm;

	public UserDetailsPanel(RemoteTLEUserService userService, RemoteUserService userCacheService)
	{
		this.userService = userService;
		this.userCacheService = userCacheService;
		setupGui();

		loadUser(null);
	}

	private void setupGui()
	{
		JLabel identifierLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.identifier")); //$NON-NLS-1$
		JLabel usernameLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.username")); //$NON-NLS-1$
		JLabel firstNameLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.first")); //$NON-NLS-1$
		JLabel lastNameLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.last")); //$NON-NLS-1$
		JLabel emailAddressLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.email")); //$NON-NLS-1$
		JLabel newPasswordLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.newpswd")); //$NON-NLS-1$
		JLabel passwordConfirmLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.confirmpswd")); //$NON-NLS-1$

		identifier = new JTextField();
		identifier.setEditable(false);
		username = new JTextField();
		firstName = new JTextField();
		lastName = new JTextField();
		emailAddress = new JTextField();
		newPassword = new JPasswordField();
		passwordConfirm = new JPasswordField();

		labels.put("username", usernameLabel);
		labels.put("firstName", firstNameLabel);
		labels.put("lastName", lastNameLabel);
		labels.put("emailAddress", emailAddressLabel);
		labels.put("email", emailAddressLabel);
		labels.put("password", newPasswordLabel);

		JButton save = new JButton(saveAction);

		final int height1 = username.getPreferredSize().height;
		final int height2 = save.getPreferredSize().height;
		final int width1 = passwordConfirmLabel.getPreferredSize().width + 10;
		final int width2 = save.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1, height1, height1, height1, height1, TableLayout.FILL,
				height2,};
		final int[] cols = {width1, TableLayout.FILL, width2,};

		setLayout(new TableLayout(rows, cols));
		add(identifierLabel, new Rectangle(0, 0, 1, 1));
		add(identifier, new Rectangle(1, 0, 2, 1));
		add(usernameLabel, new Rectangle(0, 1, 1, 1));
		add(username, new Rectangle(1, 1, 2, 1));
		add(firstNameLabel, new Rectangle(0, 2, 1, 1));
		add(firstName, new Rectangle(1, 2, 2, 1));
		add(lastNameLabel, new Rectangle(0, 3, 1, 1));
		add(lastName, new Rectangle(1, 3, 2, 1));
		add(emailAddressLabel, new Rectangle(0, 4, 1, 1));
		add(emailAddress, new Rectangle(1, 4, 2, 1));

		add(newPasswordLabel, new Rectangle(0, 6, 1, 1));
		add(newPassword, new Rectangle(1, 6, 2, 1));

		add(passwordConfirmLabel, new Rectangle(0, 7, 1, 1));
		add(passwordConfirm, new Rectangle(1, 7, 2, 1));

		add(save, new Rectangle(2, 9, 1, 1));

		changeDetector = new ChangeDetector();
		changeDetector.watch(username);
		changeDetector.watch(firstName);
		changeDetector.watch(lastName);
		changeDetector.watch(emailAddress);
		changeDetector.watch(newPassword);
		changeDetector.watch(passwordConfirm);
	}

	private final TLEAction saveAction = new SaveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			new MyGlassSwingWorker()
			{
				@Override
				public void doStuff()
				{
					saveLoadedUser();
					JOptionPane.showMessageDialog(getComponent(),
							CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.saved")); //$NON-NLS-1$
				}

				@Override
				public void finished()
				{
					loadDetails(null);
					// I am not sure what was the purpose of overriding this;
					// was it for clearChanges or loadthe details of the user or
					// anyother
				}
			}.start();
		}

		@Override
		public void update()
		{
			setEnabled(loadedUser != null);
		}
	};

	public void loadUser(final TLEUser user)
	{
		if( !changeDetector.hasDetectedChanges() )
		{
			loadDetails(user);
		}
		else
		{
			Object[] buttons = new Object[]{
					CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.save"), //$NON-NLS-1$
					CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.dontsave")}; //$NON-NLS-1$
			int results = JOptionPane.showOptionDialog(this,
					CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.confirm"), //$NON-NLS-1$
					CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.saveuser"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[1]);

			if( results == JOptionPane.NO_OPTION )
			{
				loadDetails(user);
			}
			else if( results == JOptionPane.YES_OPTION )
			{
				new MyGlassSwingWorker()
				{
					@Override
					public void doStuff()
					{
						saveLoadedUser();
						JOptionPane.showMessageDialog(getComponent(),
								CurrentLocale.get("com.tle.admin.usermanagement.internal.userdetailspanel.saved")); //$NON-NLS-1$
					}

					@Override
					public void finished()
					{
						loadDetails(user);
					}
				}.start();
			}
		}
	}

	private void loadDetails(TLEUser user)
	{
		loadedUser = user;

		boolean enabled = loadedUser != null;

		username.setEnabled(enabled);
		firstName.setEnabled(enabled);
		lastName.setEnabled(enabled);
		emailAddress.setEnabled(enabled);
		newPassword.setEnabled(enabled);
		passwordConfirm.setEnabled(enabled);

		saveAction.update();

		if( enabled )
		{
			identifier.setText(Check.nullToEmpty(user.getUuid()));
			username.setText(Check.nullToEmpty(user.getUsername()));
			firstName.setText(Check.nullToEmpty(user.getFirstName()));
			lastName.setText(Check.nullToEmpty(user.getLastName()));
			emailAddress.setText(Check.nullToEmpty(user.getEmailAddress()));
		}
		else
		{
			identifier.setText(""); //$NON-NLS-1$
			username.setText(""); //$NON-NLS-1$
			firstName.setText(""); //$NON-NLS-1$
			lastName.setText(""); //$NON-NLS-1$
			emailAddress.setText(""); //$NON-NLS-1$
		}
		newPassword.setText(""); //$NON-NLS-1$
		passwordConfirm.setText(""); //$NON-NLS-1$

		clearChanges();
	}

	/**
	 * @return true if password has changed
	 */
	private boolean saveDetails()
	{
		loadedUser.setUsername(username.getText().trim());
		loadedUser.setFirstName(firstName.getText());
		loadedUser.setLastName(lastName.getText());
		loadedUser.setEmailAddress(emailAddress.getText());

		char[] password = newPassword.getPassword();
		boolean passwordChanged = password.length > 0;
		if( passwordChanged )
		{
			loadedUser.setPassword(new String(password));
			if( !Arrays.equals(password, passwordConfirm.getPassword()) )
			{
				throw new InvalidDataException(Collections.singletonList(new ValidationError("password", CurrentLocale //$NON-NLS-1$
						.get("com.tle.admin.usermanagement.internal.userdetailspanel.mustmatch")))); //$NON-NLS-1$
			}
		}
		return passwordChanged;
	}

	public void save() throws EditorException
	{
		try
		{
			clearErrors();
			saveLoadedUser();
		}
		catch( InvalidDataException e )
		{
			ValidationError error2 = error(e);
			throw new EditorException(error2.getMessage());
		}
	}

	public void saveLoadedUser()
	{
		boolean needsHashing = saveDetails();
		String id = loadedUser.getUuid();
		if( Check.isEmpty(id) )
		{
			id = userService.add(loadedUser);
			loadedUser = userService.get(id);
		}
		else
		{
			id = userService.edit(loadedUser, needsHashing);
			userCacheService.removeFromCache(id);
		}
		loadedUser.setUuid(id);

		clearChanges();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
		super.clearChanges();
	}

	void clearErrors()
	{
		for( JLabel label : labels.values() )
		{
			String t = label.getText();
			if( t.endsWith("*") ) //$NON-NLS-1$
			{
				label.setText(t.substring(0, t.length() - 1));
			}
		}
	}

	ValidationError error(InvalidDataException ide)
	{
		ValidationError error = ide.getErrors().get(0);
		for( ValidationError e : ide.getErrors() )
		{
			JLabel label = labels.get(e.getField());
			if( label != null && !label.getText().endsWith("*") ) //$NON-NLS-1$
			{
				label.setText(label.getText() + "*"); //$NON-NLS-1$
			}
		}
		return error;
	}

	/**
	 * @author Nicholas Read
	 */
	private abstract class MyGlassSwingWorker extends GlassSwingWorker<Object>
	{
		public abstract void doStuff();

		public MyGlassSwingWorker()
		{
			setComponent(UserDetailsPanel.this);
		}

		@Override
		public Object construct()
		{
			clearErrors();
			doStuff();
			return null;
		}

		@Override
		public void exception()
		{
			Exception ex = getException();
			if( ex instanceof InvalidDataException )
			{
				JOptionPane.showMessageDialog(getComponent(), error((InvalidDataException) ex).getMessage());
			}
			else
			{
				Driver.displayError(getComponent(), "unknown", ex);
			}
		}
	}
}

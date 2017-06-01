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

package com.tle.client.harness;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;

public class LoginDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private JButton ok;
	private JButton cancel;
	private JTextField username;
	private JPasswordField password;
	private boolean okPressed;

	public LoginDialog(String server)
	{
		setup(server);
	}

	public LoginDialog(JDialog parent, String server)
	{
		super(parent);
		setup(server);
	}

	public LoginDialog(JFrame parent, String server)
	{
		super(parent);
		setup(server);
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

	/**
	 * Returns whether the OK button was pressed.
	 * 
	 * @return true if the OK button was pressed.
	 */
	public boolean isOK()
	{
		return okPressed;
	}

	private void setup(String server)
	{
		okPressed = false;

		JLabel usernameLabel = new JLabel("Username");
		JLabel passwordLabel = new JLabel("Password");

		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		username = new JTextField();
		password = new JPasswordField();

		ok.addActionListener(this);
		cancel.addActionListener(this);

		final int height1 = username.getPreferredSize().height;
		final int height2 = ok.getPreferredSize().height;
		final int width1 = Math.max(usernameLabel.getPreferredSize().width, passwordLabel.getPreferredSize().width);
		final int width2 = cancel.getPreferredSize().width;

		final int[] rows = {height1, height1, TableLayout.FILL, height2,};
		final int[] cols = {width1, TableLayout.FILL, width2, width2,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		all.add(usernameLabel, new Rectangle(0, 0, 1, 1));
		all.add(username, new Rectangle(1, 0, 3, 1));
		all.add(passwordLabel, new Rectangle(0, 1, 1, 1));
		all.add(password, new Rectangle(1, 1, 3, 1));
		all.add(ok, new Rectangle(2, 3, 1, 1));
		all.add(cancel, new Rectangle(3, 3, 1, 1));

		getContentPane().add(all);

		ok.requestFocus();

		setSize(310, 150);
		setTitle("Login to " + server);
		getRootPane().setDefaultButton(ok);
		ComponentHelper.centreOnScreen(this);
	}

	public String getUsername()
	{
		return username.getText();
	}

	public String getPassword()
	{
		return new String(password.getPassword());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == ok )
		{
			okPressed = true;
			dispose();
		}
		else if( e.getSource() == cancel )
		{
			dispose();
		}
	}
}

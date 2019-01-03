/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.client.harness;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;

@SuppressWarnings("nls")
public class AppServerEditDialog extends JDialog implements ActionListener, KeyListener
{
	private static final long serialVersionUID = 1L;

	public static final int RESULT_CANCEL = 0;
	public static final int RESULT_OK = 1;

	private int result = RESULT_CANCEL;

	private JButton ok;
	private JButton cancel;

	private JTextField profileField;
	private JTextField serverField;
	private JTextField usernameField;
	private JPasswordField passwordField;

	public AppServerEditDialog(Frame frame)
	{
		super(frame);
		setup();
	}

	public AppServerEditDialog(Frame frame, ServerProfile profile)
	{
		this(frame);
		if( profile != null )
		{
			loadConfiguration(profile);
		}
	}

	private void setup()
	{
		JLabel profileLabel = new JLabel("Profile Name:");
		JLabel serverLabel = new JLabel("Server URL:");
		JLabel usernameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");

		profileField = new JTextField();
		serverField = new JTextField();
		usernameField = new JTextField();
		passwordField = new JPasswordField();

		profileField.addKeyListener(this);
		serverField.addKeyListener(this);

		ok = new JButton("Save");
		cancel = new JButton("Cancel");

		ok.addActionListener(this);
		cancel.addActionListener(this);

		ok.setEnabled(false);

		final int width1 = profileLabel.getPreferredSize().width;
		final int width2 = cancel.getPreferredSize().width;
		final int height1 = profileField.getPreferredSize().height;
		final int height2 = cancel.getPreferredSize().height;
		final int[] rows = {height1, height1, height1, height1, TableLayout.FILL, height2,};
		final int[] cols = {width1, TableLayout.FILL, width2, width2,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(profileLabel, new Rectangle(0, 0, 1, 1));
		all.add(profileField, new Rectangle(1, 0, 3, 1));

		all.add(serverLabel, new Rectangle(0, 1, 1, 1));
		all.add(serverField, new Rectangle(1, 1, 3, 1));

		all.add(usernameLabel, new Rectangle(0, 2, 1, 1));
		all.add(usernameField, new Rectangle(1, 2, 3, 1));

		all.add(passwordLabel, new Rectangle(0, 3, 1, 1));
		all.add(passwordField, new Rectangle(1, 3, 3, 1));

		all.add(ok, new Rectangle(2, 5, 1, 1));
		all.add(cancel, new Rectangle(3, 5, 1, 1));

		setModal(true);
		setTitle("Profile Editor");
		getContentPane().add(all);
		getRootPane().setDefaultButton(ok);

		pack();
		ComponentHelper.ensureMinimumSize(this, 500, 0);
		ComponentHelper.centreOnScreen(this);
	}

	private void loadConfiguration(ServerProfile profile)
	{
		profileField.setText(profile.getName());
		serverField.setText(profile.getServer());
		usernameField.setText(profile.getUsername());
		passwordField.setText(profile.getPassword());

		ok.setEnabled(true);
	}

	public ServerProfile getProfile()
	{
		ServerProfile profile = new ServerProfile();

		profile.setName(profileField.getText());
		profile.setServer(serverField.getText());
		profile.setUsername(usernameField.getText());
		profile.setPassword(new String(passwordField.getPassword()));

		return profile;
	}

	public int getResult()
	{
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == ok )
		{
			result = RESULT_OK;
			dispose();
		}
		else if( e.getSource() == cancel )
		{
			dispose();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		boolean profileEmpty = profileField.getText().trim().length() == 0;
		boolean serverEmpty = serverField.getText().trim().length() == 0;

		ok.setEnabled(!profileEmpty && !serverEmpty);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// We do not want to listen to this event
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// We do not want to listen to this event
	}
}

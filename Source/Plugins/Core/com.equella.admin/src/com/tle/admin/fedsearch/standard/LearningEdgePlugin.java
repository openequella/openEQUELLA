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

package com.tle.admin.fedsearch.standard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.beans.search.TLESettings;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author cofarrell
 */
@SuppressWarnings("nls")
public class LearningEdgePlugin extends SearchPlugin<TLESettings> implements ActionListener
{
	private JTextField institution;
	private JTextField username;
	private JTextField secretId;
	private JPasswordField secretValue;

	private JRadioButton useCurrentUser;
	private JRadioButton useThisUser;

	public LearningEdgePlugin()
	{
		super(TLESettings.class);
	}

	@Override
	public void initGUI()
	{
		institution = new JTextField();
		secretId = new JTextField();
		secretValue = new JPasswordField();
		username = new JTextField();

		JLabel signInOptionLabel = new JLabel(s("signinoptions"));
		useCurrentUser = new JRadioButton(s("useloggedinuser"));
		useCurrentUser.addActionListener(this);
		useThisUser = new JRadioButton(s("usethisuser"));
		useThisUser.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(useCurrentUser);
		group.add(useThisUser);

		JPanel signInOptionPanel = new JPanel();
		signInOptionPanel.setLayout(new MigLayout("wrap 1, insets 0", "[fill,grow]"));
		signInOptionPanel.add(useCurrentUser);
		signInOptionPanel.add(useThisUser);
		signInOptionPanel.add(username, "gapleft 20");

		panel.add(new JLabel(s("institutionurl")));
		panel.add(institution);
		panel.add(new JLabel(s("secretid")));
		panel.add(secretId);
		panel.add(new JLabel(s("secretvalue")));
		panel.add(secretValue);
		panel.add(signInOptionLabel);
		panel.add(signInOptionPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == useCurrentUser || e.getSource() == useThisUser )
		{
			username.setEnabled(useThisUser.isSelected());
		}
	}

	private String s(String keyPart)
	{
		return getString("equella." + keyPart);
	}

	@Override
	public void load(TLESettings settings)
	{
		institution.setText(settings.getInstitutionUrl());
		secretId.setText(settings.getSharedSecretId());
		secretValue.setText(settings.getSharedSecretValue());
		username.setText(settings.getUsername());
		if( settings.isUseLoggedInUser() )
		{
			useCurrentUser.setSelected(true);
			username.setEnabled(false);
		}
		else
		{
			useThisUser.setSelected(true);
		}
	}

	@Override
	public void save(TLESettings settings)
	{
		String instURL = institution.getText();
		if( !instURL.endsWith("/") )
		{
			instURL += "/";
			institution.setText(instURL);
		}
		settings.setInstitutionUrl(instURL);
		settings.setSharedSecretId(secretId.getText());
		settings.setSharedSecretValue(new String(secretValue.getPassword()));
		settings.setUsername(username.getText());
		settings.setUseLoggedInUser(useCurrentUser.isSelected());
	}
}

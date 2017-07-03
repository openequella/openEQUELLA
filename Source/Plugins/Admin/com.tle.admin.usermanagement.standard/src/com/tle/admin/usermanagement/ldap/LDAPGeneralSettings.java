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

package com.tle.admin.usermanagement.ldap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.dytech.gui.ChangeDetector;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.applet.client.ClientService;
import com.tle.common.encryption.RemoteEncryptionService;
import com.tle.common.i18n.CurrentLocale;

import net.miginfocom.swing.MigLayout;

/**
 * Create Jan 5, 2004
 * 
 * @author Nicholas Read
 */
public class LDAPGeneralSettings extends AbstractLDAPPanel
{
	private static final long serialVersionUID = 1L;

	private static final int SEARCH_LIMIT_START = 1;
	private static final int SEARCH_LIMIT_END = Integer.MAX_VALUE;

	private JTextField urlField;
	private JComboBox versionComboBox;
	private JTextField domainField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JCheckBox forceLowercaseIds;
	private JCheckBox followBox;
	private JCheckBox wildcardsBox;
	private SpinnerNumberModel searchLimitModel;

	private RemoteEncryptionService encryptionService;

	public LDAPGeneralSettings(ClientService services)
	{
		super();
		encryptionService = services.getService(RemoteEncryptionService.class);

		createGUI();

		this.changeDetector = new ChangeDetector();

		changeDetector.watch(urlField);
		changeDetector.watch(versionComboBox);
		changeDetector.watch(searchLimitModel);
		changeDetector.watch(usernameField);
		changeDetector.watch(passwordField);
		changeDetector.watch(followBox);
		changeDetector.watch(wildcardsBox);
	}

	@Override
	@SuppressWarnings("nls")
	protected String getTabName()
	{
		return s("name");
	}

	@SuppressWarnings("nls")
	private void createGUI()
	{
		urlField = new JTextField();
		versionComboBox = new JComboBox(new String[]{"", "3", "2"});

		searchLimitModel = new SpinnerNumberModel(SEARCH_LIMIT_START, SEARCH_LIMIT_START, SEARCH_LIMIT_END, 1);
		JSpinner searchLimitField = new JSpinner(searchLimitModel);

		domainField = new JTextField();
		usernameField = new JTextField();
		passwordField = new JPasswordField();

		forceLowercaseIds = new JCheckBox();
		followBox = new JCheckBox();
		wildcardsBox = new JCheckBox();

		setLayout(new MigLayout("fillx, wrap 2", "[align label][grow]"));

		add(new JLabel(s("address")));
		add(urlField, "growx");

		add(new JLabel(s("version")));
		add(versionComboBox, "sgx 1");
		add(new JLabel(s("limit")));
		add(searchLimitField, "sgx 1");

		add(new JLabel(s("allow")));
		add(wildcardsBox);
		add(new JLabel(s("follow")));
		add(followBox);
		add(new JLabel(s("forceLowercaseIds")));
		add(forceLowercaseIds);

		add(new JLabel(s("domain")));
		add(new JLabel(s("domainprefix")), "split 2, shrink");
		add(domainField, "grow");

		add(new JLabel(s("username")));
		add(usernameField, "growx");
		add(new JLabel(s("password")));
		add(passwordField, "growx");
	}

	public void load(LDAPSettings ls)
	{
		this.settings = ls;
		urlField.setText(ls.getUrl());
		domainField.setText(ls.getDefaultDomain());
		usernameField.setText(ls.getAdminUsername());
		passwordField.setText(encryptionService.decrypt(ls.getAdminPassword()));
		searchLimitModel.setValue(ls.getSearchLimit());
		versionComboBox.setSelectedItem(ls.getVersion());
		forceLowercaseIds.setSelected(ls.isForceLowercaseIds());
		followBox.setSelected(ls.isFollow());
		wildcardsBox.setSelected(ls.isWildcards());
	}

	public void save(LDAPSettings saveSettings)
	{
		trimFields();

		saveSettings.setUrl(urlField.getText());

		saveSettings.setFollow(followBox.isSelected());
		saveSettings.setWildcards(wildcardsBox.isSelected());
		saveSettings.setForceLowercaseIds(forceLowercaseIds.isSelected());

		saveSettings.setDefaultDomain(domainField.getText());
		saveSettings.setAdminUsername(usernameField.getText());
		saveSettings.setAdminPassword(encryptionService.encrypt(new String(passwordField.getPassword())));

		saveSettings.setVersion((String) versionComboBox.getSelectedItem());
		saveSettings.setSearchLimit(searchLimitModel.getNumber().intValue());
	}

	@Override
	public void applySettings() throws Exception
	{
		save(settings);
	}

	@Override
	public boolean needsScrollPane()
	{
		return true;
	}

	private void trimFields()
	{
		urlField.setText(urlField.getText().trim());
		domainField.setText(domainField.getText().trim());
		usernameField.setText(usernameField.getText().trim());
	}

	@SuppressWarnings("nls")
	private static String s(String keypart)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.standard.ldap.general." + keypart);
	}
}

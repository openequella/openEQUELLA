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

package com.tle.admin.harvester.standard;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.harvester.EQUELLAHarvesterSettings;
import com.tle.common.harvester.RemoteEQUELLAHarvesterService;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class EQUELLAPlugin extends HarvesterPlugin<EQUELLAHarvesterSettings> implements ActionListener
{
	private static final String HARVESTER_END_POINT = "services/SoapHarvesterService";

	private JTextField serverField;
	private JTextField userField;
	private JPasswordField passField;
	private JComboBox<NameValue> collectionField;
	private JCheckBox liveOnly;

	private String selectedCollection;

	public EQUELLAPlugin()
	{
		super(EQUELLAHarvesterSettings.class);
	}

	@Override
	public void initGUI()
	{
		userField = new JTextField();
		passField = new JPasswordField();

		serverField = new JTextField();

		liveOnly = new JCheckBox();

		collectionField = new JComboBox<NameValue>();
		collectionField.setEditable(false);

		JButton getCollections = new JButton(getString("getcollections"));
		getCollections.addActionListener(this);

		final int width = getCollections.getPreferredSize().width;
		final int height = getCollections.getPreferredSize().height;

		final int[] rows = {height};
		final int[] cols = new int[]{TableLayout.FILL, width};

		JPanel collPanel = new JPanel();
		collPanel.setLayout(new TableLayout(rows, cols, 5, 5));

		collPanel.add(collectionField, new Rectangle(0, 0, 1, 1));
		collPanel.add(getCollections, new Rectangle(1, 0, 1, 1));

		panel.addComponent(new JLabel(getString("settings")));

		panel.addNameAndComponent(getString("server"), serverField);

		panel.addNameAndComponent(CurrentLocale.get("com.tle.admin.harvester.tool.detailstab.user"), userField);
		panel.addNameAndComponent(CurrentLocale.get("com.tle.admin.harvester.tool.detailstab.pass"), passField);

		panel.addNameAndComponent(getString("remotecollection"), collPanel);
		panel.addNameAndComponent(getString("live"), liveOnly);
	}

	@Override
	public void load(EQUELLAHarvesterSettings settings)
	{
		serverField.setText(settings.getServer());
		userField.setText(settings.getUser());
		passField.setText(settings.getPass());
		selectedCollection = settings.getCollection();
		String collectionName = settings.getCollectionName() == null ? selectedCollection : settings
			.getCollectionName();

		collectionField.addItem(new NameValue(collectionName, selectedCollection));
		liveOnly.setSelected(settings.isLiveOnly());
	}

	@Override
	public void save(EQUELLAHarvesterSettings settings)
	{
		String url = fixUrl(serverField.getText());
		serverField.setText(url);
		settings.setServer(url);
		settings.setUser(userField.getText());
		settings.setPass(new String(passField.getPassword()));

		settings.setLiveOnly(liveOnly.isSelected());

		NameValue selectedItem = (NameValue) collectionField.getSelectedItem();
		selectedCollection = selectedItem.getValue();
		settings.setCollection(selectedCollection);
		settings.setCollectionName(selectedItem.getName());
	}

	@Override
	@SuppressWarnings("unused")
	public void validation() throws EditorException
	{
		if( serverField.getText().isEmpty() )
		{
			throw new EditorException(getString("serverfield"));
		}

		try
		{
			String url = fixUrl(serverField.getText());
			serverField.setText(url);
			new URL(url);
		}
		catch( MalformedURLException e )
		{
			throw new EditorException(getString("serverfieldinvalid"));
		}
	}

	protected String fixUrl(String url)
	{
		String newUrl = (URLUtils.isAbsoluteUrl(url) ? url : "http://" + url);
		newUrl = newUrl.endsWith("/") ? newUrl : newUrl + "/";
		return newUrl;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateCollections(false);
	}

	private void updateCollections(final boolean clearChanges)
	{
		final URL serverUrl;
		try
		{
			String url = fixUrl(serverField.getText());
			serverField.setText(url);
			serverUrl = new URL(url);
		}
		catch( MalformedURLException e )
		{
			JOptionPane.showMessageDialog(panel.getComponent(), getString("serverfieldinvalid"));
			return;
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<List<NameValue>>()
		{
			@Override
			public List<NameValue> construct() throws Exception
			{
				collectionField.removeAllItems();
				ClientService clientService = driver.getClientService();
				RemoteEQUELLAHarvesterService equellaHarvesterService = clientService
					.getService(RemoteEQUELLAHarvesterService.class);

				List<NameValue> listCollections = null;
				try
				{
					listCollections = equellaHarvesterService.listCollections(serverUrl, userField.getText(),
						new String(passField.getPassword()), HARVESTER_END_POINT);

				}
				catch( Exception ex )
				{
					listCollections = new ArrayList<NameValue>();
					ex.printStackTrace();
					Driver.displayInformation(panel.getComponent(), getString("collectionserror", ex.getMessage()));
				}
				return listCollections;
			}

			@Override
			public void finished()
			{
				AppletGuiUtils.addItemsToJCombo(collectionField, get());
				String set = selectedCollection;
				if( !Check.isEmpty(set) )
				{
					AppletGuiUtils.selectInJCombo(collectionField, new NameValue(null, set), 0);
					if( clearChanges )
					{
						panel.clearChanges();
					}
				}
			}
		};

		worker.setComponent(collectionField);
		worker.start();
	}

	@Override
	public void validateSchema(JComboBox<NameValue> collections) throws EditorException
	{
		NameValue selectedItem = (NameValue) collectionField.getSelectedItem();
		String collection = selectedItem == null ? null : selectedItem.getValue();

		if( collection == null || collection.isEmpty() )
		{
			throw new EditorException(getString("collection"));
		}
	}

	/**
	 * @param partKey Will be prefixed with
	 *            com.tle.admin.harvester.tool.equellaplugin.
	 * @return
	 */
	private String getString(String partKey, Object... params)
	{
		return CurrentLocale.get("com.tle.admin.harvester.tool.equellaplugin." + partKey, params);
	}
}

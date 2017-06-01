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
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.harvester.OAIHarvesterSettings;
import com.tle.common.harvester.RemoteOAIHarvesterService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;

@SuppressWarnings("nls")
public class OAIPlugin extends HarvesterPlugin<OAIHarvesterSettings> implements ActionListener
{
	private JTextField serverField;

	private JComboBox<NameValue> aSetField;
	private JButton getSetsButton;

	private JComboBox<String> metaField;
	private JButton getMetaButton;

	private OAIHarvesterSettings settings;

	public OAIPlugin()
	{
		super(OAIHarvesterSettings.class);
	}

	@Override
	public void initGUI()
	{
		serverField = new JTextField();

		JPanel setsPanel = new JPanel();
		aSetField = new JComboBox<>();
		aSetField.setEditable(true);
		getSetsButton = new JButton(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.getsets"));
		getSetsButton.addActionListener(this);

		setupPanel(setsPanel, aSetField, getSetsButton);

		JPanel metaPanel = new JPanel();
		metaField = new JComboBox<>();
		metaField.setEditable(true);
		getMetaButton = new JButton(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.getmetas"));
		getMetaButton.addActionListener(this);

		setupPanel(metaPanel, metaField, getMetaButton);

		panel.addComponent(new JLabel(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.settings")));
		panel.addNameAndComponent(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.server"), serverField);
		panel.addNameAndComponent(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.format"), metaPanel);
		panel.addNameAndComponent(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.aset"), setsPanel);

	}

	private void setupPanel(JPanel aPanel, JComboBox<?> aField, JButton aButton)
	{
		final int width = aButton.getPreferredSize().width;
		final int height = aButton.getPreferredSize().height;

		final int[] rows = {height};
		int[] cols = new int[]{TableLayout.FILL, width};

		aPanel.setLayout(new TableLayout(rows, cols, 5, 5));

		aPanel.add(aField, new Rectangle(0, 0, 1, 1));
		aPanel.add(aButton, new Rectangle(1, 0, 1, 1));
	}

	@Override
	public void load(OAIHarvesterSettings settings)
	{
		this.settings = settings;
		serverField.setText(settings.getServer());

		metaField.setSelectedItem(settings.getFormat());

		aSetField.setSelectedItem(settings.getaSet());
	}

	@Override
	public void save(OAIHarvesterSettings settings)
	{
		String url = fixUrl(serverField.getText());
		serverField.setText(url);
		settings.setServer(url);
		settings.setFormat((String) metaField.getSelectedItem());

		Object selectedItem = aSetField.getSelectedItem();
		if( selectedItem instanceof NameValue )
		{
			settings.setaSet(((NameValue) selectedItem).getValue());
		}
		else
		{
			settings.setaSet((String) selectedItem);
		}
	}

	@Override
	@SuppressWarnings("unused")
	public void validation() throws EditorException
	{
		if( serverField.getText().isEmpty() )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.serverfield"));

		}
		else
		{
			try
			{
				String url = fixUrl(serverField.getText());
				serverField.setText(url);
				new URL(url);
			}
			catch( MalformedURLException e )
			{
				throw new EditorException(
					CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.serverfieldinvalid"));
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if( src == getSetsButton )
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<List<NameValue>>()
			{
				@Override
				public List<NameValue> construct() throws Exception
				{
					ClientService clientService = driver.getClientService();
					RemoteOAIHarvesterService oaiHarvesterService = clientService
						.getService(RemoteOAIHarvesterService.class);
					aSetField.removeAllItems();
					try
					{
						String url = fixUrl(serverField.getText());
						serverField.setText(url);
						return oaiHarvesterService.listSets(new URL(url));
					}
					catch( Exception ex )
					{
						Driver.displayError(null, null, ex);
						throw ex;
					}
				}

				@Override
				public void finished()
				{
					AppletGuiUtils.addItemsToJCombo(aSetField, get());
					String set = settings.getaSet();
					if( !Check.isEmpty(set) )
					{
						AppletGuiUtils.selectInJCombo(aSetField, new NameValue(null, set), 0);
						panel.clearChanges();
					}
				}
			};

			worker.setComponent(aSetField);
			worker.start();
		}
		else if( src == getMetaButton )
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<List<String>>()
			{
				@Override
				public List<String> construct() throws Exception
				{
					ClientService clientService = driver.getClientService();
					RemoteOAIHarvesterService oaiHarvesterService = clientService
						.getService(RemoteOAIHarvesterService.class);
					metaField.removeAllItems();
					try
					{
						String url = fixUrl(serverField.getText());
						serverField.setText(url);
						return oaiHarvesterService.listMetadataFormats(new URL(url));
					}
					catch( Exception ex )
					{
						Driver.displayError(null, null, ex);
						throw ex;
					}
				}

				@Override
				public void finished()
				{
					AppletGuiUtils.addItemsToJCombo(metaField, get());
					String meta = settings.getFormat();
					if( !Check.isEmpty(meta) )
					{
						AppletGuiUtils.selectInJCombo(metaField, meta, 0);
						panel.clearChanges();
					}
				}
			};

			worker.setComponent(metaField);
			worker.start();
		}
	}

	protected String fixUrl(String url)
	{
		url = url.startsWith("http://") || url.startsWith("https://") ? url : "http://" + url;
		return url;
	}

	@Override
	public void validateSchema(JComboBox<NameValue> collections) throws EditorException
	{
		String collection = ((NameValue) collections.getSelectedItem()).getValue();
		ItemDefinition itemDef = driver.getClientService().getService(RemoteItemDefinitionService.class)
			.getByUuid(collection);

		RemoteSchemaService schemaService = driver.getClientService().getService(RemoteSchemaService.class);
		Schema schema = schemaService.get(itemDef.getSchema().getId());
		PropBagEx definition = schema.getDefinitionNonThreadSafe();

		boolean nodeExists = false;
		String nodeLoc = "item/oai/id";
		if( definition.nodeExists(nodeLoc) )
		{
			Map<String, String> attributesForNode = definition.getAttributesForNode(nodeLoc);

			if( attributesForNode != null && "true".equalsIgnoreCase(attributesForNode.get("attribute"))
				&& "true".equalsIgnoreCase(attributesForNode.get("field")) )
			{
				nodeExists = true;
			}
		}

		if( !nodeExists )
		{
			JOptionPane.showMessageDialog(panel.getComponent(),
				CurrentLocale.get("com.tle.admin.harvester.tool.oaiplugin.schema"));
		}

	}
}

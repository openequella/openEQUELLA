/*
 * Copyright 2019 Apereo
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

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.common.harvester.AbstractTLFHarvesterSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;

@SuppressWarnings("nls")
public abstract class AbstractTLFPlugin<T extends AbstractTLFHarvesterSettings>
	extends
		HarvesterPlugin<T>
{
	private JTextField userField;
	private JPasswordField passField;

	private JCheckBox liveOnly;
	private JCheckBox harvestLearningObjects;
	private JCheckBox harvestResources;

	public AbstractTLFPlugin(Class<T> clazz)
	{
		super(clazz);
	}

	protected abstract String getPluginsFieldString();

	@Override
	public void initGUI()
	{
		userField = new JTextField();
		passField = new JPasswordField();

		liveOnly = new JCheckBox();
		harvestLearningObjects = new JCheckBox();
		harvestResources = new JCheckBox();

		panel.addComponent(new JLabel(getString(getPluginsFieldString())));
		panel.addNameAndComponent(getString("detailstab.user"), userField);
		panel.addNameAndComponent(getString("detailstab.pass"), passField);

		panel.addNameAndComponent(getString("loraxplugin.live"), liveOnly);
		panel.addNameAndComponent(getString("loraxplugin.harvestlo"),
			harvestLearningObjects);
		panel.addNameAndComponent(getString("loraxplugin.harvestre"),
			harvestResources);

	}

	@Override
	public void load(T settings)
	{

		userField.setText(settings.getUser());
		passField.setText(settings.getPass());

		liveOnly.setSelected(settings.getLiveOnly());
		harvestLearningObjects.setSelected(settings.getHarvestLearningObjects());
		harvestResources.setSelected(settings.getHarvestResources());

	}

	@Override
	public void save(T settings)
	{
		settings.setUser(userField.getText());
		settings.setPass(new String(passField.getPassword()));

		settings.setLiveOnly(liveOnly.isSelected());
		settings.setHarvestLearningObjects(harvestLearningObjects.isSelected());
		settings.setHarvestResources(harvestResources.isSelected());
	}

	@Override
	public void validation() throws EditorException
	{
		if( userField.getText().isEmpty() )
		{
			throw new EditorException(getString("loraxplugin.userfield"));
		}

		if( !harvestLearningObjects.isSelected() && !harvestResources.isSelected() )
		{
			throw new EditorException(getString("loraxplugin.harvest"));
		}
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
		String nodeLoc = "item/itembody/tlfid";
		if( definition.nodeExists(nodeLoc) )
		{
			Map<String, String> attributesForNode = definition.getAttributesForNode(nodeLoc);

			if( attributesForNode != null && "true".equalsIgnoreCase(attributesForNode.get("field")) )
			{
				nodeExists = true;
			}
		}

		if( !nodeExists )
		{
			JOptionPane.showMessageDialog(panel.getComponent(),
					getString("loraxplugin.schema"));
		}

	}
}

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

package com.tle.admin.harvester.tool;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.harvester.standard.HarvesterPlugin;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.HarvesterProfileSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class HarvesterDetailsTab extends BaseEntityTab<HarvesterProfile>
	implements
		AbstractDetailsTab<HarvesterProfile>
{
	private String k(String key)
	{
		return getKey("harvestdetails."+key);
	}

	private String getLocaleString(String key)
	{
		return getString("harvestdetails."+key);
	}

	private HarvesterPlugin<?> plugin;
	protected JNameValuePanel namePanel;

	protected I18nTextField nameField;
	private final JCheckBox enabled;
	protected JSeparator separate1;
	protected JSeparator separate2;
	protected JComboBox<NameValue> collections;
	protected JComboBox<String> transforms;
	protected JRadioButton newVersionRadio;
	protected JRadioButton updateVersionRadio;

	public HarvesterDetailsTab()
	{
		nameField = new I18nTextField(BundleCache.getLanguages());
		enabled = new JCheckBox();
		separate1 = new JSeparator();
		separate2 = new JSeparator();

		namePanel = new JNameValuePanel();
		collections = new JComboBox<>();
		transforms = new JComboBox<>();

		ButtonGroup radioGroup = new ButtonGroup();
		newVersionRadio = new JRadioButton(getLocaleString("createnew"));
		updateVersionRadio = new JRadioButton(getLocaleString("updatecurrent"));

		radioGroup.add(newVersionRadio);
		radioGroup.add(updateVersionRadio);

		namePanel.addTextAndComponent(k("namefield"),
			nameField);
		namePanel.addTextAndComponent(k("enabled"), enabled);
		namePanel.addTextAndComponent(k("collections"),
			collections);
		namePanel.addTextAndComponent(k("import"), transforms);

		namePanel.addComponent(separate1);

		namePanel.addComponent(new JLabel(getLocaleString("updates")));
		namePanel.addTextAndComponent(k("emptyfiller"), new JLabel(getLocaleString("versioningdescriptor")));

		namePanel.addTextAndComponent(k("emptyfiller"), newVersionRadio);
		namePanel.addTextAndComponent(k("emptyfiller"), updateVersionRadio);

		namePanel.addComponent(separate2);

	}

	@Override
	public void setDriver(Driver driver)
	{
		super.setDriver(driver);
		plugin.setDriver(driver);
	}

	@Override
	public final void init(Component parent)
	{
		TableLayout layout = new TableLayout(new int[]{TableLayout.FILL}, new int[]{TableLayout.DOUBLE_FILL,
				TableLayout.FILL});
		layout.setColumnSize(1, 0);
		setLayout(layout);
		add(namePanel.getComponent(), new Rectangle(0, 0, 1, 1));
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		nameField.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final HarvesterProfile havProfile = state.getEntity();

		final HarvesterProfileSettings settings = plugin.newInstance();
		settings.load(havProfile);

		nameField.load(havProfile.getName());

		Boolean enabled2 = havProfile.getEnabled();
		if( enabled2 != null )
		{
			enabled.setSelected(enabled2);
		}
		else
		{
			enabled.setSelected(true);
		}

		// NewVersionOnHarvest flag is true by default
		Boolean newVersionOnHarvest = havProfile.getNewVersionOnHarvest();
		if( newVersionOnHarvest != null )
		{
			newVersionRadio.setSelected(newVersionOnHarvest);
			updateVersionRadio.setSelected(!newVersionOnHarvest);
		}
		else
		{
			newVersionRadio.setSelected(true);
			updateVersionRadio.setSelected(false);
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<List<NameValue>>()
		{
			@Override
			public List<NameValue> construct() throws Exception
			{
				List<BaseEntityLabel> cols = clientService.getService(RemoteItemDefinitionService.class).listAll();

				List<NameValue> nvs = BundleCache.getNameUuidValues(cols);
				Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
				return nvs;
			}

			@Override
			public void finished()
			{
				AppletGuiUtils.addItemsToJCombo(collections, get());

				String collection = havProfile.getAttribute("itemDef");
				if( !Check.isEmpty(collection) )
				{
					AppletGuiUtils.selectInJCombo(collections, new NameValue(null, collection), 0);
					namePanel.clearChanges();
				}
				updateTransforms(havProfile, true);
				collections.addItemListener(new ItemListener()
				{
					@Override
					public void itemStateChanged(ItemEvent e)
					{
						updateTransforms(havProfile, false);
					}
				});
			}
		};
		worker.setComponent(collections);
		worker.start();

		plugin.loadSettings(state.getEntityPack(), settings);
	}

	private void updateTransforms(final HarvesterProfile havProfile, final boolean clearChanges)
	{
		Object selectedItem = collections.getSelectedItem();
		final String uuid;
		if( selectedItem instanceof NameValue )
		{
			uuid = ((NameValue) selectedItem).getValue();
		}
		else
		{
			uuid = (String) selectedItem;
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<List<String>>()
		{
			@Override
			public List<String> construct() throws Exception
			{
				long schemaId = clientService.getService(RemoteItemDefinitionService.class)
					.getSchemaIdForCollectionUuid(uuid);
				List<String> importSchemaTypes = clientService.getService(RemoteSchemaService.class)
					.getImportSchemaTypes(schemaId);

				return importSchemaTypes;
			}

			@Override
			public void finished()
			{
				String selectedTransform = (String) transforms.getSelectedItem();
				transforms.removeAllItems();
				if( selectedTransform == null )
				{
					selectedTransform = havProfile.getAttribute("schemaInputTransform");
				}
				List<String> items = get();
				items.add(0, null);
				AppletGuiUtils.addItemsToJCombo(transforms, items);
				AppletGuiUtils.selectInJCombo(transforms, selectedTransform, 0);
				if( clearChanges )
				{
					namePanel.clearChanges();
				}

			}
		};

		worker.setComponent(transforms);
		worker.start();

	}

	@Override
	public void save()
	{
		final HarvesterProfile havProfile = state.getEntity();

		final HarvesterProfileSettings settings = plugin.newInstance();
		settings.setName(nameField.save());
		havProfile.setEnabled(enabled.isSelected());
		// At this stage, we're dealing with a either/or on the version on
		// harvest flag/button,
		// and hence RadioGroup semantics to guarantee mutual exclUsivity of
		// newVersion/updateVersion
		havProfile.setNewVersionOnHarvest(newVersionRadio.isSelected());
		havProfile.setAttribute("itemDef", ((NameValue) collections.getSelectedItem()).getValue());
		havProfile.setAttribute("schemaInputTransform", ((String) transforms.getSelectedItem()));
		plugin.saveSettings(settings);
		settings.save(havProfile);
	}

	@Override
	public String getTitle()
	{
		return getLocaleString("name");
	}

	@Override
	public void validation() throws EditorException
	{
		if( nameField.isCompletelyEmpty() )
		{
			throw new EditorException(getLocaleString("entername"));
		}
		if( collections.getSelectedItem() == null || collections.getSelectedItem().toString().isEmpty() )
		{
			throw new EditorException(getLocaleString("collection"));
		}

		plugin.validation();
		plugin.validateSchema(collections);
	}

	public void setPlugin(HarvesterPlugin<?> plugin)
	{
		this.plugin = plugin;
		plugin.setPanel(namePanel);
		plugin.initGUI();
	}

}
